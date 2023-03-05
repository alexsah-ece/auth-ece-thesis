// generic
desc keyspaces;
use metrics;
desc tables;

// --------------- Electricity

// 1-minute queries
select * from metrics.metrics_aggregates_60
    where gateway='1'
    and metric='ELECTRICITY'
    and attribute='VOLTAGE'
    and bucket='2006-12-17';

select * from metrics.metrics_aggregates_60
    where gateway='1'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER'
    and bucket='2006-12-17'
    and windowstart >= '2006-12-17 00:00:00'
    and windowstart <= '2006-12-17 00:10:00';

select * from metrics.metrics_aggregates_60
    where gateway='1'
    and metric='ELECTRICITY'
    and attribute='INTENSITY'
    and bucket='2006-12-17'
    and windowstart >= '2006-12-17 00:00:00'
    and windowstart <= '2006-12-17 00:10:00';


// 10-minute queries
select * from metrics.metrics_aggregates_600
    where gateway='2'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER'
    and bucket='2006-12-17';

select * from metrics.metrics_aggregates_600
    where gateway='2'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER'
    and bucket='2006-12-17'
    and windowstart >= '2006-12-17 03:00:00'
    and windowstart <= '2006-12-17 04:00:00';

// 1-hour queries
select * from metrics.metrics_aggregates_3600
    where gateway='4'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER'
    and bucket='2006-12-01';

select * from metrics.metrics_aggregates_3600
    where gateway='4'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER'
    and bucket='2006-12-01'
    and windowstart >= '2006-12-17 03:00:00'
    and windowstart <= '2006-12-17 07:00:00';


// 1-day queries
select * from metrics.metrics_aggregates_86400
    where gateway='4'
    and metric='ELECTRICITY'
    and attribute='ACTIVE_POWER';



// --------------- Water

// 10-minute queries
select * from metrics.metrics_aggregates_600
    where gateway='3'
    and metric='WATER'
    and attribute='WATER_IDD'
    and bucket='2012-04-01';

// 1-hour queries
select * from metrics.metrics_aggregates_3600
    where gateway='0'
    and metric='WATER'
    and attribute='WATER_SUMMATION'
    and bucket='2012-04-01';

