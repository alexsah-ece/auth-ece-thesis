CREATE KEYSPACE IF NOT EXISTS metrics WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS metrics.metrics_aggregates_60 (
    gateway text,
    metric text,
    attribute text,
    bucket timestamp,
    windowStart timestamp,
    min double,
    max double,
    avg double,
    sampleCount bigint,
    PRIMARY KEY ((gateway, metric, attribute, bucket), windowStart)
) WITH CLUSTERING ORDER BY (windowStart DESC);

CREATE TABLE IF NOT EXISTS metrics.metrics_aggregates_600 (
    gateway text,
    metric text,
    attribute text,
    bucket timestamp,
    windowStart timestamp,
    min double,
    max double,
    avg double,
    sampleCount bigint,
    PRIMARY KEY ((gateway, metric, attribute, bucket), windowStart)
) WITH CLUSTERING ORDER BY (windowStart DESC);

CREATE TABLE IF NOT EXISTS metrics.metrics_aggregates_3600 (
    gateway text,
    metric text,
    attribute text,
    bucket timestamp,
    windowStart timestamp,
    min double,
    max double,
    avg double,
    sampleCount bigint,
    PRIMARY KEY ((gateway, metric, attribute, bucket), windowStart)
) WITH CLUSTERING ORDER BY (windowStart DESC);

CREATE TABLE IF NOT EXISTS metrics.metrics_aggregates_86400 (
    gateway text,
    metric text,
    attribute text,
    windowStart timestamp,
    min double,
    max double,
    avg double,
    sampleCount bigint,
    PRIMARY KEY ((gateway, metric, attribute), windowStart)
) WITH CLUSTERING ORDER BY (windowStart DESC);
