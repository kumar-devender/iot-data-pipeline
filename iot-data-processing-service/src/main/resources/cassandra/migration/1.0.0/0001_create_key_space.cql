CREATE KEYSPACE IF NOT EXISTS iot_data_store WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
