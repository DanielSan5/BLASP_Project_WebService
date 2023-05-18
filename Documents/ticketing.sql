-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Creato il: Mag 18, 2023 alle 23:24
-- Versione del server: 10.4.27-MariaDB
-- Versione PHP: 8.1.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ticketing`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `avviso`
--

CREATE TABLE `avviso` (
  `AVV_id` int(11) NOT NULL,
  `AVV_descrizione` varchar(254) NOT NULL,
  `UT_id_avvisante` int(11) NOT NULL,
  `TIC_id_avvisato` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `avviso`
--

INSERT INTO `avviso` (`AVV_id`, `AVV_descrizione`, `UT_id_avvisante`, `TIC_id_avvisato`) VALUES
(1, 'blah blah', 16, 2),
(2, 'blah blah', 16, 2),
(3, 'blah blah', 16, 2);

-- --------------------------------------------------------

--
-- Struttura della tabella `indirizzo_scolastico`
--

CREATE TABLE `indirizzo_scolastico` (
  `INS_id` int(11) NOT NULL,
  `INS_nome` varchar(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `indirizzo_scolastico`
--

INSERT INTO `indirizzo_scolastico` (`INS_id`, `INS_nome`) VALUES
(1, 'IT'),
(2, 'MM'),
(3, 'EE'),
(4, 'GC'),
(5, 'CM');

-- --------------------------------------------------------

--
-- Struttura della tabella `localita`
--

CREATE TABLE `localita` (
  `LC_id` int(11) NOT NULL,
  `LC_descrizione` varchar(254) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `localita`
--

INSERT INTO `localita` (`LC_id`, `LC_descrizione`) VALUES
(1, 'Granarolo'),
(2, 'Castel maggiore');

-- --------------------------------------------------------

--
-- Struttura della tabella `materia`
--

CREATE TABLE `materia` (
  `MAT_id` int(11) NOT NULL,
  `MAT_nome` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `materia`
--

INSERT INTO `materia` (`MAT_id`, `MAT_nome`) VALUES
(1, 'Lingua e letteratura italiana'),
(2, 'Storia'),
(3, 'Lingua Inglese'),
(4, 'Matematica'),
(5, 'Scienze motorie e sportive'),
(6, 'Diritto ed Economia'),
(7, 'Scienze della terra e biologia'),
(8, 'Fisica'),
(9, 'Chimica'),
(10, 'Tecnologie e tecniche di rappresentazione grafica'),
(11, 'Geografia'),
(12, 'Tecnologie informatiche'),
(13, 'Meccanica, macchine ed energia'),
(14, 'Sistemi e automazione'),
(15, 'Tecnologie meccaniche di processo e prodotto'),
(16, 'Disegno, progettazione e organizzazione industriale'),
(17, 'Tecnologie e progettazione di sistemi elettrici ed elettronici'),
(18, 'Elettrotecnica ed elettronica'),
(19, 'Sistemi automatici'),
(20, 'Sistemi e Reti'),
(21, 'Tecnologie e progettazione di sistemi informatici e di telecomunicazioni'),
(22, 'Informatica'),
(23, 'Telecomunicazioni'),
(24, 'Gestione progetto, organizzazione d\'impresa'),
(25, 'Teoria della comunicazione'),
(26, 'Progettazione multimediale'),
(27, 'Tecnologie dei processi di produzione'),
(28, 'Organizzazione e gestione dei processi produttivi'),
(29, 'Laboratori tecnici'),
(30, 'Chimica analitica e strumentale'),
(31, 'Chimica organica e biochimica'),
(32, 'Tecnologie chimiche industriali');

-- --------------------------------------------------------

--
-- Struttura della tabella `preferiti`
--

CREATE TABLE `preferiti` (
  `PR_id` int(11) NOT NULL,
  `UT_id` int(11) NOT NULL,
  `TIC_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `preferiti`
--

INSERT INTO `preferiti` (`PR_id`, `UT_id`, `TIC_id`) VALUES
(2, 16, 2);

-- --------------------------------------------------------

--
-- Struttura della tabella `segnalazione`
--

CREATE TABLE `segnalazione` (
  `SEG_id` int(11) NOT NULL,
  `SEG_descrizione` varchar(254) NOT NULL,
  `UT_id_segnalato` int(11) NOT NULL,
  `UT_id_segnalatore` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `segnalazione`
--

INSERT INTO `segnalazione` (`SEG_id`, `SEG_descrizione`, `UT_id_segnalato`, `UT_id_segnalatore`) VALUES
(1, 'blah blah', 16, 16),
(2, 'blah blah', 16, 16),
(3, 'blah blah', 16, 16);

-- --------------------------------------------------------

--
-- Struttura della tabella `tickets`
--

CREATE TABLE `tickets` (
  `TIC_id` int(11) NOT NULL,
  `TIC_stato` varchar(254) NOT NULL DEFAULT 'open',
  `TIC_materia` varchar(254) NOT NULL,
  `TIC_tags` varchar(254) NOT NULL,
  `TIC_descrizione` varchar(254) NOT NULL,
  `UT_id_apertura` int(11) NOT NULL,
  `UT_id_accettazione` int(11) DEFAULT NULL,
  `TIC_data_creazione` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `tickets`
--

INSERT INTO `tickets` (`TIC_id`, `TIC_stato`, `TIC_materia`, `TIC_tags`, `TIC_descrizione`, `UT_id_apertura`, `UT_id_accettazione`, `TIC_data_creazione`) VALUES
(2, 'open', 'Fisica', 'terza', 'bla bla bla', 16, NULL, '2023-05-18'),
(4, 'open', 'Matematica', 'prima', 'blah blah', 16, NULL, '2023-05-18'),
(5, 'open', 'Matematica', 'prima', 'blah blah', 16, NULL, '2023-05-18'),
(6, 'open', 'Matematica', 'prima', 'blah blah', 16, NULL, '2023-05-18'),
(7, 'open', 'Matematica', 'prima', 'blah blah', 16, NULL, '2023-05-18'),
(8, 'open', 'Matematica', 'prima', 'blah blah', 16, NULL, '2023-05-18'),
(9, 'open', 'Matematica', 'prima,seconda', 'blah blah', 16, NULL, '2023-05-18');

-- --------------------------------------------------------

--
-- Struttura della tabella `utenti`
--

CREATE TABLE `utenti` (
  `UT_id` int(11) NOT NULL,
  `UT_nome` varchar(154) NOT NULL,
  `UT_cognome` varchar(154) NOT NULL,
  `UT_email` varchar(255) NOT NULL,
  `UT_password` varchar(255) NOT NULL,
  `UT_classe` int(11) NOT NULL,
  `UT_indirizzo_scolastico` varchar(154) NOT NULL,
  `UT_descrizione` varchar(154) DEFAULT NULL,
  `UT_data_nascita` date NOT NULL,
  `UT_localita` varchar(254) NOT NULL,
  `UT_stato` varchar(10) NOT NULL DEFAULT 'none',
  `UT_ver_code` varchar(36) DEFAULT NULL,
  `UT_admin` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `utenti`
--

INSERT INTO `utenti` (`UT_id`, `UT_nome`, `UT_cognome`, `UT_email`, `UT_password`, `UT_classe`, `UT_indirizzo_scolastico`, `UT_descrizione`, `UT_data_nascita`, `UT_localita`, `UT_stato`, `UT_ver_code`, `UT_admin`) VALUES
(16, 'Nico', 'Nieri', 'nico.nieri@aldini.istruzioneer.it', '$argon2id$v=19$m=1048576,t=4,p=8$lfNMZwfnpYVn/yWsq4poWA$Kk0eDDKIYIAmmoIsBv3NhYqG8TEhwj8F0JRmDGrk8+E', 3, 'IT', 'mi piace imparare', '2004-03-10', 'Granarolo', 'none', NULL, 1),
(17, 'Daniele', 'Reatti', 'daniele.reatti@aldini.istruzioneer.it', '$argon2id$v=19$m=1048576,t=4,p=8$n5X7uiSeqevSn8jkZH88ow$pL3xLHfzuQojA39+eZIp8KSG66tntj6M8ByA+VVdqWQ', 5, 'MM', NULL, '2004-03-10', 'Granarolo', 'none', 'da257245-dc79-4e8d-8f61-71688c4daf39', 0);

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `avviso`
--
ALTER TABLE `avviso`
  ADD PRIMARY KEY (`AVV_id`),
  ADD KEY `UT_id_avvisante` (`UT_id_avvisante`),
  ADD KEY `TIC_id_avvisato` (`TIC_id_avvisato`);

--
-- Indici per le tabelle `indirizzo_scolastico`
--
ALTER TABLE `indirizzo_scolastico`
  ADD PRIMARY KEY (`INS_id`);

--
-- Indici per le tabelle `localita`
--
ALTER TABLE `localita`
  ADD PRIMARY KEY (`LC_id`);

--
-- Indici per le tabelle `materia`
--
ALTER TABLE `materia`
  ADD PRIMARY KEY (`MAT_id`);

--
-- Indici per le tabelle `preferiti`
--
ALTER TABLE `preferiti`
  ADD PRIMARY KEY (`PR_id`),
  ADD KEY `TIC_id` (`TIC_id`),
  ADD KEY `UT_id` (`UT_id`);

--
-- Indici per le tabelle `segnalazione`
--
ALTER TABLE `segnalazione`
  ADD PRIMARY KEY (`SEG_id`),
  ADD KEY `id_segnalato` (`UT_id_segnalato`),
  ADD KEY `id_segnalatore` (`UT_id_segnalatore`);

--
-- Indici per le tabelle `tickets`
--
ALTER TABLE `tickets`
  ADD PRIMARY KEY (`TIC_id`),
  ADD KEY `UT_id_accettazione` (`UT_id_accettazione`),
  ADD KEY `UT_id_apertura` (`UT_id_apertura`);

--
-- Indici per le tabelle `utenti`
--
ALTER TABLE `utenti`
  ADD PRIMARY KEY (`UT_id`);

--
-- AUTO_INCREMENT per le tabelle scaricate
--

--
-- AUTO_INCREMENT per la tabella `avviso`
--
ALTER TABLE `avviso`
  MODIFY `AVV_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT per la tabella `indirizzo_scolastico`
--
ALTER TABLE `indirizzo_scolastico`
  MODIFY `INS_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT per la tabella `localita`
--
ALTER TABLE `localita`
  MODIFY `LC_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT per la tabella `materia`
--
ALTER TABLE `materia`
  MODIFY `MAT_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT per la tabella `preferiti`
--
ALTER TABLE `preferiti`
  MODIFY `PR_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT per la tabella `segnalazione`
--
ALTER TABLE `segnalazione`
  MODIFY `SEG_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT per la tabella `tickets`
--
ALTER TABLE `tickets`
  MODIFY `TIC_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT per la tabella `utenti`
--
ALTER TABLE `utenti`
  MODIFY `UT_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `avviso`
--
ALTER TABLE `avviso`
  ADD CONSTRAINT `avviso_ibfk_1` FOREIGN KEY (`UT_id_avvisante`) REFERENCES `utenti` (`UT_id`),
  ADD CONSTRAINT `avviso_ibfk_2` FOREIGN KEY (`TIC_id_avvisato`) REFERENCES `tickets` (`TIC_id`);

--
-- Limiti per la tabella `preferiti`
--
ALTER TABLE `preferiti`
  ADD CONSTRAINT `preferiti_ibfk_1` FOREIGN KEY (`TIC_id`) REFERENCES `tickets` (`TIC_id`),
  ADD CONSTRAINT `preferiti_ibfk_2` FOREIGN KEY (`UT_id`) REFERENCES `utenti` (`UT_id`);

--
-- Limiti per la tabella `segnalazione`
--
ALTER TABLE `segnalazione`
  ADD CONSTRAINT `id_segnalato` FOREIGN KEY (`UT_id_segnalato`) REFERENCES `utenti` (`UT_id`),
  ADD CONSTRAINT `id_segnalatore` FOREIGN KEY (`UT_id_segnalatore`) REFERENCES `utenti` (`UT_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
