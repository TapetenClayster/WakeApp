-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jun 17, 2022 at 08:12 AM
-- Server version: 10.4.18-MariaDB
-- PHP Version: 8.0.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wakeapp_db`
--
CREATE DATABASE IF NOT EXISTS `wakeapp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wakeapp_db`;

-- --------------------------------------------------------

--
-- Table structure for table `T_Locations`
--

DROP TABLE IF EXISTS `T_Locations`;
CREATE TABLE `T_Locations` (
  `p_location_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `street` varchar(50) DEFAULT NULL,
  `housenumber` varchar(50) DEFAULT NULL,
  `postalcode` varchar(50) DEFAULT NULL,
  `region` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `longitude` varchar(50) NOT NULL,
  `latitude` varchar(50) NOT NULL,
  `bvgId` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `T_Waketimes`
--

DROP TABLE IF EXISTS `T_Waketimes`;
CREATE TABLE `T_Waketimes` (
  `p_waketime_id` int(11) NOT NULL,
  `arrivaltime` time NOT NULL,
  `prepduration` int(11) NOT NULL,
  `transporttype` enum('CAR','CYCLE','BVG','ON_FOOT','WHEELCHAIR') DEFAULT NULL,
  `f_location_id1` int(11) DEFAULT NULL,
  `f_location_id2` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `T_Locations`
--
ALTER TABLE `T_Locations`
  ADD PRIMARY KEY (`p_location_id`);

--
-- Indexes for table `T_Waketimes`
--
ALTER TABLE `T_Waketimes`
  ADD PRIMARY KEY (`p_waketime_id`),
  ADD KEY `Loc1` (`f_location_id1`),
  ADD KEY `Loc2` (`f_location_id2`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `T_Waketimes`
--
ALTER TABLE `T_Waketimes`
  ADD CONSTRAINT `Loc1` FOREIGN KEY (`f_location_id1`) REFERENCES `T_Locations` (`p_location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `Loc2` FOREIGN KEY (`f_location_id2`) REFERENCES `T_Locations` (`p_location_id`) ON DELETE SET NULL ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
