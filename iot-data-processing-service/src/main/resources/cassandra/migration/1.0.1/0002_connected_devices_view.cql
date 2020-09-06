CREATE TABLE IF NOT EXISTS iot_data_store.connected_devices
(
    id       uuid primary key,
    protocol varchar,
    total    int
);