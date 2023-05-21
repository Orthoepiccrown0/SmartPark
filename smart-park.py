import flask
import base64
import os
import io
import time
import subprocess
import json
import random
import string
import calendar
import secrets
from flask_jwt_extended import create_access_token
from flask_jwt_extended import get_jwt_identity
from flask_jwt_extended import jwt_required
from flask_jwt_extended import JWTManager
from flask_jwt_extended import get_jwt
from datetime import date
from datetime import datetime
from flask import request, Response, jsonify
from flask_mysqldb import MySQL
from PIL import Image
from io import BytesIO

app = flask.Flask(__name__)
app.config['JWT_SECRET_KEY'] = "KEY"
jwt = JWTManager(app)


app.config["DEBUG"] = True
app.config['MYSQL_HOST'] = 'HOST'
app.config['MYSQL_USER'] = 'admin'
app.config['MYSQL_PASSWORD'] = 'PASSWORD'
app.config['MYSQL_DB'] = 'smartpark'
mysql = MySQL(app)


# CLASSES
class CarPlates:
    def __init__(self, idCar, idUser, plate, balanceUser):
        self.idCar = idCar
        self.idUser = idUser
        self.plate = plate
        self.balanceUser = balanceUser


class User:
    def __init__(self, idUser, phone):
        self.idUser = idUser
        self.phone = phone


"""class Parks:
    def __init__(self, idPark, name, description, price, minTime, slots, visible):
        self.idPark = idPark
        self.name = name
        self.description = description
        self.price = price
        self."""


# ERROR CODES
# BE001 => car plate with idUser already exists
# BE002 => when adding a new car: plate with idUser already exists
# BE003 => car balance is not 0, car can't enter.
# BE004 => car can enter, but the user should register.

# /////////////////////////////////////////////////////////////
# Customer side
# Login Or Register User With Car Plate queries
Q_USER_QUERY = "SELECT * FROM user WHERE phone = %s"
Q_USER_REGISTER = "INSERT INTO user (phone) VALUES (%s)"
Q_INSERT_NEW_CAR_WITH_USER = "INSERT INTO car (idUser, plate) VALUES (%s, %s)"
Q_LAST_INSERTED_USER = "SELECT * FROM user WHERE id = %s"
Q_CHECK_IF_CARPLATE_EXISTS = "SELECT * FROM car WHERE plate = %s"
Q_UPDATE_CAR_USER = "UPDATE car SET idUser = %s WHERE plate = %s"
Q_CHECK_IF_PLATE_HAS_USERID = "SELECT COALESCE(idUser, 0) FROM car WHERE plate = %s"

# Get user with all cars
Q_GET_USER_WITH_CARS = "SELECT user.*, car.plate, car.balance FROM user LEFT JOIN car ON user.id = car.idUser WHERE user.id = %s"


# Homepage queries
Q_GET_DEBIT_AND_NUM_CARS = ("SELECT SUM(DISTINCT c.balance) AS debit, COUNT(DISTINCT c.id) AS numCars, COUNT(l.id) AS numParkedTimes "
                            "FROM car AS c "
                            "LEFT JOIN lot AS l ON c.id = l.idCar "
                            "WHERE c.idUser = %s")

Q_GET_TOTAL_SPENT = ("SELECT SUM(pp.import) "
                     "FROM pendingpayments AS pp "
                     "LEFT JOIN transactions AS t ON pp.id = t.idPendingPayment "
                     "LEFT JOIN lot AS l ON pp.idLot = l.id "
                     "LEFT JOIN car AS c ON l.idCar = c.id "
                     "WHERE c.idUser = %s "
                     "AND t.success = 1 "
                     "UNION ALL "
                     "SELECT SUM(pp.import) "
                     "FROM pendingpayments AS pp "
                     "LEFT JOIN transactions AS t ON pp.id = t.idPendingPayment "
                     "LEFT JOIN lot AS l ON pp.idLot = l.id "
                     "LEFT JOIN car AS c ON l.idCar = c.id "
                     "WHERE c.idUser = %s "
                     "AND DATE(t.time) > %s "
                     "AND DATE(t.time) < %s "
                     "AND t.success = 1"
                     )

Q_GET_AVAILABLE_PAYMENTS = ("SELECT DISTINCT pp.id, pp.import, l.inTimestamp, l.outTimestamp, p.name, c.plate "
                            "FROM transactions AS t "
                            "LEFT JOIN pendingpayments AS pp ON t.idPendingPayment <> pp.id "
                            "LEFT JOIN lot AS l ON pp.idLot = l.id "
                            "LEFT JOIN park AS p ON l.idPark = p.id "
                            "LEFT JOIN car AS c ON l.idCar = c.id "
                            "WHERE c.idUser = %s "
                             "AND pp.id NOT IN (SELECT idPendingPayment FROM transactions) ")

Q_GET_ACTIVE_CARS = ("SELECT c.plate, l.inTimestamp, p.price, p.name "
                     "FROM lot AS l "
                     "LEFT JOIN car AS c ON l.idCar = c.id "
                     "LEFT JOIN park AS p ON l.idPark = p.id "
                     "WHERE c.idUser = %s "
                     "AND outTimestamp IS NULL ")

Q_GET_LAST_TRANSACTIONS = ("SELECT pp.import, l.inTimestamp, l.outTimestamp, p.name, c.plate "
                           "FROM lot AS l "
                           "LEFT JOIN pendingpayments AS pp ON l.id = pp.idLot "
                           "LEFT JOIN transactions AS t ON pp.id = t.idPendingPayment "
                           "LEFT JOIN car AS c ON l.idCar = c.id "
                           "LEFT JOIN park AS p ON l.idPark = p.id "
                           "WHERE c.idUser = %s "
                           "AND t.success = 1 ")


# History page queries
# for pending payments was used Q_GET_AVAILABLE_PAYMENTS

Q_GET_SUCCEED_PAYMENTS = ("SELECT pp.import, l.inTimestamp, l.outTimestamp, p.name, c.plate "
                            "FROM transactions AS t "
                            "LEFT JOIN pendingpayments AS pp ON t.idPendingPayment = pp.id "
                            "LEFT JOIN lot AS l ON pp.idLot = l.id "
                            "LEFT JOIN park AS p ON l.idPark = p.id "
                            "LEFT JOIN car AS c ON l.idCar = c.id "
                            "WHERE c.idUser = %s "
                            "AND t.success = 1")

Q_GET_FAILED_PAYMENTS = ("SELECT pp.import, l.inTimestamp, l.outTimestamp, p.name, c.plate "
                            "FROM transactions AS t "
                            "LEFT JOIN pendingpayments AS pp ON t.idPendingPayment = pp.id "
                            "LEFT JOIN lot AS l ON pp.idLot = l.id "
                            "LEFT JOIN park AS p ON l.idPark = p.id "
                            "LEFT JOIN car AS c ON l.idCar = c.id "
                            "WHERE c.idUser = %s "
                            "AND t.success = 0")


# /////////////////////////////////////////////////////////////
# Park manager side
Q_INSERT_NEW_ZONE = "INSERT INTO zone (name, description) VALUES (%s, %s)"

Q_INSERT_NEW_PARK = "INSERT INTO park (idZone, name, price, minTime, slots) VALUES (%s, %s, %s, %s, %s)"

Q_GET_ALL_ZONES = "SELECT id, name, description FROM zone WHERE visible = 1"

Q_FIND_IDLOT_FROM_CARPLATE = ("SELECT l.id "
                              "FROM lot AS l "
                              "LEFT JOIN car AS c ON l.idCar = c.id "
                              "WHERE c.plate = %s "
                              "AND l.outTimestamp IS NULL ")

Q_GET_IDCAR_FROM_CARPLATE = "SELECT id FROM car WHERE plate = %s"

Q_UPDATE_OUTTIMESTAMP = "UPDATE lot SET outTimestamp = %s WHERE id = %s"

Q_CREATE_PENDINGPAYMENT = "INSERT INTO pendingpayments (idLot, import) VALUES (%s, %s)"

Q_SELECT_CAR_BALANCE = "SELECT balance FROM car WHERE plate = %s"

Q_UPDATE_CAR_BALANCE = "UPDATE car SET balance = %s WHERE plate = %s"

Q_GET_ALL_PARKS_FOR_ZONE = ("SELECT id, name, price, minTime, slots, visible "
                            "FROM park "
                            "WHERE idZone = %s ")

Q_CHECK_CAR_IN_PARK = ("SELECT COALESCE(l.id, 0), inTimestamp "
                       "FROM lot AS l "
                       "LEFT JOIN car AS c ON l.idCar = c.id "
                       "WHERE c.plate = %s "
                       "AND outTimestamp IS NULL ")

Q_GET_PARK_PRICE_FROM_ID = "SELECT price FROM park WHERE id = %s "


Q_INSERT_LOT = "INSERT INTO lot (idPark, idCar, inTimestamp, type) VALUES (%s, %s, %s, %s)"

Q_INSERT_CAR = "INSERT INTO car (plate) VALUES (%s)"

# Q_CHECK_ZONE_EXISTS = "SELECT name, description FROM zone WHERE "


# /////////////////////////////////////////////////////////////


# Transcations
Q_INSERT_TRANSACTION = "INSERT INTO transactions (idPendingPayment, time, success) VALUES (%s, %s, %s)"


# Prova
Q_PROVA = "SELECT * FROM user WHERE id > 49"


def getFilename(length):
   letters = string.ascii_lowercase
   return ''.join(random.choice(letters) for i in range(length))


@app.route('/api/process-data', methods=['POST'])
def process():
    if 'image' in request.json and request.json['image'] is not None:
        data = request.json['image']

        # Save the image file to disk
        filename = "img/"+getFilename(10)+".jpg"
        bytes_decoded = base64.b64decode(data)
        img = Image.open(BytesIO(bytes_decoded))
        img.show()
        # to jpg
        out_jpg = img.convert("RGB")

        # save file
        out_jpg.save(filename)
        command = ["alpr", filename, "-c", "eu", "-j"]
        result = subprocess.check_output(command)
        os.remove(filename)
        r = Response(response=result, status=200, mimetype="application/json")
        r.headers["Content-Type"] = "application/json; charset=utf-8"
        return r
    else:
        result = {"success": False, "message": "Field image is required!"}
        r = Response(result=json.dumps(result), status=417,
                     mimetype="application/json")
        r.headers["Content-Type"] = "application/json; charset=utf-8"
        return r


# Client Side
@app.route('/api/login', methods=['POST'])
def login():
    phone = request.json['phone']
    plate = request.json['plate']
    cursor = mysql.connection.cursor()
    cursor.execute(Q_USER_QUERY, (phone,))
    results = cursor.fetchone()
    if results is None:
        cursor.execute(Q_USER_REGISTER, (phone,))
        last_id = cursor.lastrowid
        if last_id is not None:
            cursor.execute(Q_CHECK_IF_CARPLATE_EXISTS, (plate,))
            checkCarPlate = cursor.fetchall()
            if len(checkCarPlate) == 0:
                cursor.execute(Q_INSERT_NEW_CAR_WITH_USER, (last_id, plate,))
                mysql.connection.commit()
                cursor.execute(Q_LAST_INSERTED_USER, (last_id,))
                lastInsertedUser = cursor.fetchone()
                cursor.close()
                user = {
                    "idUser": lastInsertedUser[0], "phone": lastInsertedUser[1]}
                access_token = create_access_token(
                    identity=user['idUser'], expires_delta=False, additional_claims=user)
                return jsonify({"idUser": user['idUser'], "phone": user['phone'], "token": access_token})
            else:
                cursor.execute(Q_CHECK_IF_PLATE_HAS_USERID, (plate,))
                checkPlateIdUser = cursor.fetchone()
                if checkPlateIdUser[0] == 0:
                    setNewUserToCar = cursor.execute(
                        Q_UPDATE_CAR_USER, (last_id, plate,))
                    mysql.connection.commit()
                    cursor.execute(Q_LAST_INSERTED_USER, (last_id,))
                    lastInsertedUser = cursor.fetchone()
                    cursor.close()
                    user = {
                        "idUser": lastInsertedUser[0], "phone": lastInsertedUser[1]}
                    access_token = create_access_token(
                        identity=user['idUser'], expires_delta=False, additional_claims=user)
                    return jsonify({"idUser": user['idUser'], "phone": user['phone'], "token": access_token})

                else:
                    return jsonify({"success": False,
                                    "code": "BE001"}), 414

        else:
            return jsonify({"success": False,
                            "code": "BE321"}), 500
    else:
        cursor.close()
        user = {"idUser": results[0], "phone": results[1]}
        access_token = create_access_token(
            identity=user['idUser'], expires_delta=False, additional_claims=user)
        return jsonify({"idUser": user['idUser'], "phone": user['phone'], "token": access_token})


@app.route('/api/addCar', methods=['POST'])
@jwt_required()
def addCar():
    """idUser = request.json["idUser"]"""
    idUser = get_jwt_identity()
    plate = request.json["plate"]
    cursor = mysql.connection.cursor()
    cursor.execute(Q_CHECK_IF_CARPLATE_EXISTS, (plate,))
    result = cursor.fetchone()
    # if car doesn't exist
    if result is None:
        # create new car with idUser from input
        cursor.execute(Q_INSERT_NEW_CAR_WITH_USER, (idUser, plate,))
        mysql.connection.commit()
        cursor.close()
        return jsonify({"success": True})
    else:
        # check if car has idUser
        carClass = CarPlates(result[0], result[1], result[2], result[3])
        if carClass.idUser is None:
            # if not add idUser to this car
            cursor.execute(Q_UPDATE_CAR_USER, (idUser, plate,))
            mysql.connection.commit()
            cursor.close()
            return jsonify({"success": True})
        else:
            # else error
            return jsonify({"success": False,
                            "code": "BE002"}), 414


@app.route('/api/userInfo', methods=['GET'])
@jwt_required()
def getUserInfo():
    """idUser = request.json["idUser"]"""
    idUser = get_jwt_identity()
    cursor = mysql.connection.cursor()
    cursor.execute(Q_GET_USER_WITH_CARS, (idUser,))
    result = cursor.fetchall()
    phone = result[0][1]
    userInfoDict = [{
                    "plate": item[2],
                    "balance": item[3],
                    } for item in result]

    return jsonify({"idUser": idUser,
                    "phone": phone,
                    "cars": userInfoDict})


@app.route('/api/getHomepage', methods=['GET'])
@jwt_required()
def getHomepage():
    idUser = get_jwt_identity()
    cursor = mysql.connection.cursor()
    # User debit, totalCarNumbers
    cursor.execute(Q_GET_DEBIT_AND_NUM_CARS, (idUser,))
    result = cursor.fetchone()
    otherInfo = {
        "debit": result[0],
        "numCars": result[1],
        "numParkedTimes": result[2],
    }

    # User total spent
    # Calc first and last day of the current month
    dateOfNow = date.today()
    firstDayOfMonth = date(
        dateOfNow.year, dateOfNow.month, 1).strftime('%Y-%m-%d')
    lastDayOfMonthSearch = calendar.monthrange(dateOfNow.year, dateOfNow.month)
    lastDayOfMonthNumber = lastDayOfMonthSearch[1]
    lastDayOfMonth = date(dateOfNow.year, dateOfNow.month,
                          lastDayOfMonthNumber).strftime('%Y-%m-%d')
    """ return jsonify({"Firstday: ": firstDayOfMonth,
                    "LastDay: ": lastDayOfMonth}) """
    cursor.execute(Q_GET_TOTAL_SPENT, (idUser, idUser,
                   firstDayOfMonth, lastDayOfMonth,))
    result = cursor.fetchall()
    totalSpent = result[0][0]
    spentThisMonth = result[1][0]

    # User available payments
    cursor.execute(Q_GET_AVAILABLE_PAYMENTS, (idUser,))
    result = cursor.fetchall()
    availablePayments = [{
        "idPendingPayment": availablePaymentQueryResult[0],
        "import": availablePaymentQueryResult[1],
        "carDateIn": availablePaymentQueryResult[2].strftime('%Y-%m-%d %H:%M:%S'),
        "carDateOut": availablePaymentQueryResult[3].strftime('%Y-%m-%d %H:%M:%S'),
        "parkName": availablePaymentQueryResult[4],
        "plate": availablePaymentQueryResult[5],
    }for availablePaymentQueryResult in result]

    # Active cars
    cursor.execute(Q_GET_ACTIVE_CARS, (idUser,))
    result = cursor.fetchall()
    activeCars = [{
        "plate": activeCarInPark[0],
        "enterTimestamp": activeCarInPark[1].strftime('%Y-%m-%d %H:%M:%S'),
        "parkPrice": activeCarInPark[2],
        "parkName": activeCarInPark[3],
    } for activeCarInPark in result]

    # Last transactions
    cursor.execute(Q_GET_LAST_TRANSACTIONS, (idUser,))
    result = cursor.fetchall()
    lastTransactions = [{
        "import": transaction[0],
        "carDateIn": transaction[1].strftime('%Y-%m-%d %H:%M:%S'),
        "carDateOut": transaction[2].strftime('%Y-%m-%d %H:%M:%S'),
        "parkName": transaction[3],
        "plate": transaction[4],
    }for transaction in result]

    return jsonify({"otherInfo": otherInfo,
                    "totalSpent": totalSpent,
                    "spentThisMonth": spentThisMonth,
                    "availablePayments": availablePayments,
                    "activeCars": activeCars,
                    "lastTransactions": lastTransactions})


@app.route('/api/getPaymentHistory', methods=['GET'])
@jwt_required()
def getPaymentHistory():
    idUser = get_jwt_identity()
    cursor = mysql.connection.cursor()
    # Get pending payments
    cursor.execute(Q_GET_AVAILABLE_PAYMENTS, (idUser,))
    result = cursor.fetchall()
    pendingPayments = [{
        "idPendingPayment": pendingPaymentsQueryResult[0],
        "import": pendingPaymentsQueryResult[1],
        "carDateIn": pendingPaymentsQueryResult[2].strftime('%Y-%m-%d %H:%M:%S'),
        "carDateOut": pendingPaymentsQueryResult[3].strftime('%Y-%m-%d %H:%M:%S'),
        "parkName": pendingPaymentsQueryResult[4],
        "plate": pendingPaymentsQueryResult[5],
    }for pendingPaymentsQueryResult in result]

    # Get succeed payments
    cursor.execute(Q_GET_SUCCEED_PAYMENTS, (idUser,))
    result = cursor.fetchall()
    succeedPayments = [{
        "import": succeedPaymentQueryResult[0],
        "carDateIn": succeedPaymentQueryResult[1].strftime('%Y-%m-%d %H:%M:%S'),
        "carDateOut": succeedPaymentQueryResult[2].strftime('%Y-%m-%d %H:%M:%S'),
        "parkName": succeedPaymentQueryResult[3],
        "plate": succeedPaymentQueryResult[4],
    }for succeedPaymentQueryResult in result]

    # Get failed payments
    cursor.execute(Q_GET_FAILED_PAYMENTS, (idUser,))
    result = cursor.fetchall()
    failedPayments = [{
        "import": failedPaymentsQueryResult[0],
        "carDateIn": failedPaymentsQueryResult[1].strftime('%Y-%m-%d %H:%M:%S'),
        "carDateOut": failedPaymentsQueryResult[2].strftime('%Y-%m-%d %H:%M:%S'),
        "parkName": failedPaymentsQueryResult[3],
        "plate": failedPaymentsQueryResult[4],
    }for failedPaymentsQueryResult in result]

    return jsonify({"pendingPayments": pendingPayments,
                    "succeedPayments": succeedPayments,
                    "failedPayments": failedPayments})


# /////////////////////////////////////////////////////////////
# Park manager side


@app.route('/api/addLotWithCar', methods=['POST'])
def addLotWithCar():
    idPark = request.json['idPark']
    plate = request.json['plate']
    dateTimeNow = datetime.now()
    cursor = mysql.connection.cursor()
    cursor.execute(Q_CHECK_CAR_IN_PARK, (plate,))
    result = cursor.fetchone()
    if result is None:
        cursor.execute(Q_SELECT_CAR_BALANCE, (plate,))
        resultQuerySelectBalance = cursor.fetchone()
        balance = resultQuerySelectBalance
        if resultQuerySelectBalance is None:
            cursor.execute(Q_INSERT_CAR, (plate,))
            idCar = cursor.lastrowid
        else:
            if balance[0] > 0:
                return jsonify({"success": False,
                                "code": "BE003"})
            cursor.execute(Q_GET_IDCAR_FROM_CARPLATE, (plate,))
            result = cursor.fetchone()

            if result is not None:
                idCar = result
            else:
                cursor.execute(Q_INSERT_CAR, (plate,))
                idCar = cursor.lastrowid

        cursor.execute(Q_INSERT_LOT, (idPark, idCar, dateTimeNow, "GENERIC",))
        mysql.connection.commit()
        cursor.close()
        return jsonify({"success": True,"code": "BE004"})
    else:
        dateDiff = dateTimeNow - result[1]
        cursor.execute(Q_GET_PARK_PRICE_FROM_ID, (idPark,))
        qResultParkPrice = cursor.fetchone()
        parkPrice = qResultParkPrice[0]
        totalPriceForParking = dateDiff.total_seconds() * float((parkPrice / 60) / 60)
        roundedPrice = round(totalPriceForParking, 2)
        resultFunc = createPendingPayment(plate, dateTimeNow, roundedPrice)
        return resultFunc


    return jsonify({"success": False})




#Car out
def createPendingPayment(plate, carOutTimestamp, toPay):
    """ plate = request.json['plate']
    carOutTimestamp = request.json['carOutTimestamp']
    toPay = request.json['toPay']"""
    cursor = mysql.connection.cursor()

    #Find idLot using plate
    cursor.execute(Q_FIND_IDLOT_FROM_CARPLATE, (plate,))
    result = cursor.fetchone()
    idLot = result[0]

    #Update outTimestamp
    cursor.execute(Q_UPDATE_OUTTIMESTAMP, (carOutTimestamp, idLot,))

    #Take car balance
    cursor.execute(Q_SELECT_CAR_BALANCE, (plate,))
    resultQuerySelectBalance = cursor.fetchone()
    balance = resultQuerySelectBalance[0]
    balanceNew = float(balance) + toPay

    #Update car balance
    cursor.execute(Q_UPDATE_CAR_BALANCE, (balanceNew, plate,))

    #Insert new pending payment for this lot
    cursor.execute(Q_CREATE_PENDINGPAYMENT, (idLot, toPay,))
    mysql.connection.commit()

    cursor.close()
    return jsonify({"success": True})


@app.route('/api/addZone', methods=['POST'])
def addZone():
    zoneName = request.json['zoneName']
    zoneDescription = request.json['zoneDescription']
    cursor = mysql.connection.cursor()
    cursor.execute(Q_INSERT_NEW_ZONE, (zoneName, zoneDescription,))
    last_id = cursor.lastrowid
    mysql.connection.commit()
    cursor.close()
    return jsonify({"success": True,
                    "idZone": last_id,
                    "zoneName": zoneName,
                    "zoneDescription": zoneDescription})


@app.route('/api/addPark', methods=['POST'])
def addpark():
    zoneId = request.json['zoneId']
    parkName = request.json['parkName']
    price = request.json['price']
    minTime = request.json['minTime']
    slots = request.json['slots']
    cursor = mysql.connection.cursor()
    cursor.execute(Q_INSERT_NEW_PARK, (zoneId, parkName, price, minTime, slots,))
    last_id = cursor.lastrowid
    mysql.connection.commit()
    cursor.close()
    return jsonify({"success": True,
                    "idPark": last_id,
                    "parkName": parkName})



@app.route('/api/successPayment', methods=['POST'])
def successPayment():
    idPendingPayment = request.json['idPendingPayment']
    paymentTime = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    #code for real payment
    paymentSuccess = True

    createTransaction(idPendingPayment, paymentTime, paymentSuccess)

    return jsonify({"success": paymentSuccess})

"""@app.route('/api/failedPayment', methods=['POST'])
def failedPayment():
    return jsonify({"success": False})"""



def createTransaction(idPendingpayment, paymentTime, paymentSuccess):
    cursor = mysql.connection.cursor()
    if paymentSuccess is True:
        pDB = 1
    else:
        pDB = 0
    cursor.execute(Q_INSERT_TRANSACTION, (idPendingpayment, paymentTime, pDB,))
    mysql.connection.commit()
    return 1

@app.route('/api/getAllZones', methods=['GET'])
def getAllZones():
    cursor = mysql.connection.cursor()
    cursor.execute(Q_GET_ALL_ZONES)
    result = cursor.fetchall()
    zones = [{
        "idZone": zone[0],
        "zoneName": zone[1],
        "zoneDescription": zone[2],
        "parks": getAllZonesHelper(zone[0])
    } for zone in result]

    cursor.close()
    return jsonify({"zones": zones})




def getAllZonesHelper(idZone):
    cursor = mysql.connection.cursor()
    cursor.execute(Q_GET_ALL_PARKS_FOR_ZONE, (idZone,))
    resultParks = cursor.fetchall()
    parks = [{
        "idPark": park[0],
        "name": park[1],
        "price": park[2],
        "minTime": park[3],
        "slots": park[4],
        "visible": park[5]
    } for park in resultParks]
    return parks


app.run(host="0.0.0.0", port=443)