CREATE TABLE `metrics` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `shortcut` VARCHAR(45) NOT NULL,
  `provider` VARCHAR(45) NOT NULL,
  `technology` VARCHAR(45) NOT NULL,
  `started` TIMESTAMP NULL,
  `finished` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

  CREATE TABLE `datasets` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `created` TIMESTAMP NOT NULL,
  `emails` VARCHAR(1024) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

  CREATE TABLE `edges` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NULL,
  `source` INT NOT NULL,
  `destination` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

  CREATE TABLE `nodes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));


ALTER TABLE `metrics`
ADD COLUMN `dataset_id` INT NOT NULL AFTER `finished`,
ADD INDEX `metric_dataset_id_idx` (`dataset_id` ASC);
ALTER TABLE `metrics`
ADD CONSTRAINT `metric_dataset_id`
  FOREIGN KEY (`dataset_id`)
  REFERENCES `datasets` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

  ALTER TABLE `metrics`
CHANGE COLUMN `dataset_id` `dataset_id` INT(11) NOT NULL AFTER `id`;


ALTER TABLE `edges`
ADD COLUMN `dataset_id` INT NOT NULL AFTER `id`,
ADD INDEX `edge_dataset_id_idx` (`dataset_id` ASC);
ALTER TABLE `edges`
ADD CONSTRAINT `edge_dataset_id`
  FOREIGN KEY (`dataset_id`)
  REFERENCES `datasets` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

  ALTER TABLE `edges`
ADD INDEX `source_node_id_idx` (`source` ASC),
ADD INDEX `destination_node_id_idx` (`destination` ASC);
ALTER TABLE `edges`
ADD CONSTRAINT `source_node_id`
  FOREIGN KEY (`source`)
  REFERENCES `nodes` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `destination_node_id`
  FOREIGN KEY (`destination`)
  REFERENCES `nodes` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



  ALTER TABLE `nodes`
ADD COLUMN `dataset_id` INT NOT NULL AFTER `id`,
ADD COLUMN `risc_score` DECIMAL NULL AFTER `name`,
ADD INDEX `node_dataset_id_idx` (`dataset_id` ASC);
ALTER TABLE `nodes`
ADD CONSTRAINT `node_dataset_id`
  FOREIGN KEY (`dataset_id`)
  REFERENCES `datasets` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


CREATE TABLE `attributes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `edge_id` INT NULL,
  `node_id` INT NULL,
  `dataset_id` INT NULL,
  `key` VARCHAR(64) NULL,
  `value` VARCHAR(1024) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `edge_attribute_id_idx` (`edge_id` ASC),
  INDEX `node_attribute_id_idx` (`node_id` ASC),
  INDEX `dataset_attribute_id_idx` (`dataset_id` ASC),
  CONSTRAINT `edge_attribute_id`
    FOREIGN KEY (`edge_id`)
    REFERENCES `edges` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `node_attribute_id`
    FOREIGN KEY (`node_id`)
    REFERENCES `nodes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `dataset_attribute_id`
    FOREIGN KEY (`dataset_id`)
    REFERENCES `datasets` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
