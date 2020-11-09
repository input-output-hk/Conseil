#!/bin/bash

################################################################
##
##   PostgresQL Database Creation Script
##   Last Update: Nov 09, 2020
##   Mantis
##
################################################################

echo -e " # MANTIS-INDEXER-API - DB SCRIPT # \n"

# Default schema
script="$(pwd)/sql/mantis.sql"
echo "- .sql file path: $script"

# The db configs should be the same as the ones in the config
# file used to run lorre/the API.
user="foo"
pass="'bar'"
db="conseil"

################################################################

echo -e "- About to create $db database and $user user \n"

sudo -u postgres psql << EOF
    DROP DATABASE IF EXISTS $db;
    DROP USER IF EXISTS $user;
    CREATE DATABASE $db;
    CREATE USER $user WITH ENCRYPTED PASSWORD $pass;
    GRANT ALL PRIVILEGES ON DATABASE $db TO $user;
    \c $db
    \i /$script;
    GRANT ALL PRIVILEGES ON SCHEMA mantis TO $user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA mantis TO $user;
EOF


echo "Finished running db creation for mantis"

### End of script ####