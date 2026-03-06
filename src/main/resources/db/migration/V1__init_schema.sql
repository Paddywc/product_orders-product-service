CREATE TABLE `product` (
                           `created_at` datetime(6) NOT NULL,
                           `price_usc_cents` bigint(20) NOT NULL,
                           `updated_at` datetime(6) NOT NULL,
                           `version` bigint(20) DEFAULT NULL,
                           `product_id` binary(16) NOT NULL,
                           `product_description` varchar(2000) DEFAULT NULL,
                           `product_name` varchar(255) NOT NULL,
                           `product_category` enum('BOOKS','CLOTHING','ELECTRONICS','FOOD','HOME') NOT NULL,
                           `product_status` enum('ACTIVE','INACTIVE') NOT NULL,
                           PRIMARY KEY (`product_id`)
);