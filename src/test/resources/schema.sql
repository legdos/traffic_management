CREATE TABLE cargo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cargo_name VARCHAR(255),
  cargo_unit VARCHAR(255)
);

CREATE TABLE factory (
  id INT AUTO_INCREMENT PRIMARY KEY,
  factory_name VARCHAR(255) NOT NULL,
  factory_address VARCHAR(255),
  factory_latitude DECIMAL(10,6),
  factory_longitude DECIMAL(10,6),
  need_id INT,
  deleted_at TIMESTAMP(3)
);

CREATE TABLE need (
  id INT AUTO_INCREMENT PRIMARY KEY,
  need_name VARCHAR(255),
  cargo_id INT,
  cargo_total_weight DECIMAL(10,2),
  factory_id INT,
  required_vehicle_type VARCHAR(80),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  deleted_at TIMESTAMP(3),
  CONSTRAINT fk_test_need_factory FOREIGN KEY (factory_id) REFERENCES factory(id),
  CONSTRAINT fk_test_need_cargo FOREIGN KEY (cargo_id) REFERENCES cargo(id)
);

CREATE TABLE poi (
  id INT AUTO_INCREMENT PRIMARY KEY,
  poi_name VARCHAR(255),
  poi_type VARCHAR(255),
  poi_address VARCHAR(255),
  poi_latitude DECIMAL(10,6),
  poi_longitude DECIMAL(10,6),
  deleted_at TIMESTAMP(3)
);

CREATE TABLE route (
  id INT PRIMARY KEY,
  road_name VARCHAR(255),
  road_type VARCHAR(255),
  distance DECIMAL(10,2),
  duration VARCHAR(255),
  poi_id INT,
  CONSTRAINT fk_test_route_poi FOREIGN KEY (poi_id) REFERENCES poi(id)
);

CREATE TABLE vehicle (
  id INT AUTO_INCREMENT PRIMARY KEY,
  vehicle_type VARCHAR(255),
  max_load VARCHAR(255),
  vehicle_number INT,
  vehicle_user VARCHAR(255),
  vehicle_fuel_level VARCHAR(255),
  vehicle_latitude DECIMAL(10,6),
  vehicle_longitude DECIMAL(10,6),
  license_number VARCHAR(32),
  current_action_code VARCHAR(40),
  status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
  version BIGINT NOT NULL DEFAULT 0,
  route_id INT,
  segment_index INT,
  segment_progress DECIMAL(8,5) NOT NULL DEFAULT 0,
  position_at TIMESTAMP(3),
  deleted_at TIMESTAMP(3)
);

CREATE TABLE trans_task (
  id INT AUTO_INCREMENT PRIMARY KEY,
  trans_task_name VARCHAR(255),
  need_id INT,
  start_time VARCHAR(255),
  duration VARCHAR(255),
  route_id INT,
  vehicle_id INT,
  trans_task_accident_rate VARCHAR(255),
  status VARCHAR(30) NOT NULL DEFAULT 'LEGACY',
  match_score DECIMAL(8,5),
  active_need_id INT UNIQUE,
  active_vehicle_id INT UNIQUE,
  completed_at TIMESTAMP(3),
  CONSTRAINT fk_test_task_need FOREIGN KEY (need_id) REFERENCES need(id),
  CONSTRAINT fk_test_task_route FOREIGN KEY (route_id) REFERENCES route(id),
  CONSTRAINT fk_test_task_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);

CREATE TABLE action_definition (
  code INT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  action_type VARCHAR(40) NOT NULL,
  default_duration_sec INT NOT NULL DEFAULT 60,
  accident_rate DECIMAL(8,6) NOT NULL DEFAULT 0
);
