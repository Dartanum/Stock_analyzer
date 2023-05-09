create table model (
    figi        varchar primary key,
    start_date  date not null,
    is_pause    bool default false
);

create table regression_model (
    id          uuid primary key,
    figi        varchar references model,
    start_date  date not null,
    end_date    date,
    b0          float not null,
    b1          float not null,
    deviation   float not null
);

create table wave_model (
    id              uuid primary key,
    regression_id   uuid references regression_model,
    start_date      date not null,
    end_date        date,
    frequency       float,
    amplitude       float,
    phase           float,
    period          float,
    correlation     float
);