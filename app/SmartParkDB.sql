CREATE TABLE `zones` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `visible` boolean DEFAULT true
);

CREATE TABLE `parks` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `minTime` integer NOT NULL,
  `slots` integer NOT NULL,
  `visible` boolean DEFAULT true
);

CREATE TABLE `lots` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `inTimestamp` timestamp NOT NULL DEFAULT (now()),
  `outTimestamp` timestamp,
  `type` varchar(255)
);

CREATE TABLE `users` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `phone` varchar(255) NOT NULL
);

CREATE TABLE `cars` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `plate` varchar(255) NOT NULL,
  `balance` double DEFAULT 0
);

CREATE TABLE `transactions` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `time` timestamp,
  `success` boolean
);

CREATE TABLE `pendingPayments` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `import` double NOT NULL
);

ALTER TABLE `pendingPayments` ADD FOREIGN KEY (`id`) REFERENCES `transactions` (`id`);

ALTER TABLE `lots` ADD FOREIGN KEY (`id`) REFERENCES `pendingPayments` (`id`);

ALTER TABLE `parks` ADD FOREIGN KEY (`id`) REFERENCES `zones` (`id`);

ALTER TABLE `cars` ADD FOREIGN KEY (`id`) REFERENCES `lots` (`id`);

ALTER TABLE `cars` ADD FOREIGN KEY (`id`) REFERENCES `users` (`id`);

ALTER TABLE `lots` ADD FOREIGN KEY (`id`) REFERENCES `parks` (`id`);
