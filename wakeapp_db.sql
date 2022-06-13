-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jun 13, 2022 at 09:01 PM
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
-- Table structure for table `T_Waketimes`
--

DROP TABLE IF EXISTS `T_Waketimes`;
CREATE TABLE `T_Waketimes` (
  `p_waketime_id` int(11) NOT NULL,
  `arrivaltime` time NOT NULL,
  `travelduration` int(11) NOT NULL,
  `prepduration` int(11) NOT NULL,
  `transporttype` enum('ON_FOOT','CYCLE','OVPN','CAR') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `T_Waketimes`
--
ALTER TABLE `T_Waketimes`
  ADD PRIMARY KEY (`p_waketime_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `T_Waketimes`
--
ALTER TABLE `T_Waketimes`
  MODIFY `p_waketime_id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
