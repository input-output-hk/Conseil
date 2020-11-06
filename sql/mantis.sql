--
-- PostgreSQL database dump
--

-- Dumped from database version 11.4
-- Dumped by pg_dump version 11.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

CREATE SCHEMA mantis;

-- Table is based on eth_getBlockByHash from https://eth.wiki/json-rpc/API
CREATE TABLE mantis.blocks (
  hash text NOT NULL PRIMARY KEY,
  number integer NOT NULL,
  difficulty text NOT NULL,
  extra_data text NOT NULL,
  gas_limit text NOT NULL,
  gas_used text NOT NULL,
  logs_bloom text NOT NULL,
  miner text NOT NULL,
  nonce text NOT NULL,
  parent_hash text,
  receipts_root text NOT NULL,
  sha3_uncles text NOT NULL,
  size text NOT NULL,
  state_root text NOT NULL,
  total_difficulty text NOT NULL,
  transactions_root text NOT NULL,
  uncles text,
  timestamp timestamp without time zone NOT NULL
);

-- Table is based on eth_getTransactionByHash from https://eth.wiki/json-rpc/API
CREATE TABLE mantis.transactions (
  hash text NOT NULL PRIMARY KEY,
  nonce text NOT NULL,
  block_hash text NOT NULL,
  block_number integer NOT NULL,
  transaction_index text NOT NULL,
  "from" text NOT NULL,
  "to" text,
  value numeric NOT NULL, -- value in wei
  gas_price text NOT NULL,
  gas text NOT NULL,
  input text NOT NULL,
  pending boolean,
  is_outgoing boolean
);

ALTER TABLE ONLY mantis.transactions
  ADD CONSTRAINT mantis_transactions_block_hash_fkey FOREIGN KEY (block_hash) REFERENCES mantis.blocks(hash);

-- Table is based on eth_getTransactionReceipt from https://eth.wiki/json-rpc/API
CREATE TABLE mantis.receipts (
  transaction_hash text NOT NULL,
  transaction_index text NOT NULL,
  block_number integer NOT NULL,
  block_hash text NOT NULL,
  cumulative_gas_used text NOT NULL,
  gas_used text NOT NULL,
  contract_address text
);

-- Table is based on eth_getLogs from https://eth.wiki/json-rpc/API
CREATE TABLE mantis.logs (
  address text NOT NULL,
  block_hash text NOT NULL,
  block_number integer NOT NULL,
  data text NOT NULL,
  log_index text NOT NULL,
  removed boolean NOT NULL,
  topics text NOT NULL,
  transaction_hash text NOT NULL,
  transaction_index text NOT NULL
);

ALTER TABLE ONLY mantis.logs
  ADD CONSTRAINT mantis_logs_block_hash_fkey FOREIGN KEY (block_hash) REFERENCES mantis.blocks(hash);

CREATE TABLE mantis.contracts (
  address text NOT NULL,
  block_hash text NOT NULL,
  block_number integer NOT NULL,
  bytecode text NOT NULL,
  is_erc20 boolean NOT NULL DEFAULT false,
  is_erc721 boolean NOT NULL DEFAULT false
);

CREATE TABLE mantis.tokens (
  address text NOT NULL,
  block_hash text NOT NULL,
  block_number integer NOT NULL,
  name text NOT NULL,
  symbol text NOT NULL,
  decimals text NOT NULL,
  total_supply text NOT NULL
);

CREATE TABLE mantis.token_transfers (
  block_number integer NOT NULL,
  transaction_hash text NOT NULL,
  from_address text NOT NULL,
  to_address text NOT NULL,
  value numeric NOT NULL
);

CREATE OR REPLACE VIEW mantis.accounts AS
SELECT
  "to" AS address,
  SUM(value) AS value
FROM
  mantis.transactions
GROUP BY
  "to";