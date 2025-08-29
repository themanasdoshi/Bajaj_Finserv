-- Drop if exist
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

-- Department table
CREATE TABLE department (
    department_id INT PRIMARY KEY,
    department_name VARCHAR(50) NOT NULL
);

-- Employee table
CREATE TABLE employee (
    emp_id INT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(10),
    department INT,
    FOREIGN KEY (department) REFERENCES department(department_id)
);

-- Payments table
CREATE TABLE payments (
    payment_id INT PRIMARY KEY,
    emp_id INT,
    amount DECIMAL(12,2),
    payment_time TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employee(emp_id)
);
