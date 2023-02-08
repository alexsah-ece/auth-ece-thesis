CREATE KEYSPACE IF NOT EXISTS metrics WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS metrics.metrics_minutely (
    gateway text,
    metric text,
    attribute text,
    bucket timestamp,
    minute timestamp,
    min double,
    max double,
    avg double,
    PRIMARY KEY ((gateway, metric, attribute, bucket), minute)
) WITH CLUSTERING ORDER BY (minute DESC);


CREATE TABLE IF NOT EXISTS metrics.metrics_hourly (
    gateway text,
    metric text,
    attribute text,
    bucket timestamp,
    hour timestamp,
    min double,
    max double,
    avg double,
    PRIMARY KEY ((gateway, metric, attribute, bucket), hour)
) WITH CLUSTERING ORDER BY (hour DESC);


CREATE TABLE IF NOT EXISTS metrics.metrics_monthly (
    gateway text,
    metric text,
    attribute text,
    month timestamp,
    min double,
    max double,
    avg double,
    PRIMARY KEY ((gateway, metric, attribute), month)
) WITH CLUSTERING ORDER BY (month DESC);
