SELECT e.emp_id,
       e.first_name,
       e.last_name,
       d.department_name,
       COALESCE((
         SELECT COUNT(*)
         FROM employee e2
         WHERE e2.department = e.department
           AND e2.dob > e.dob
       ), 0) AS younger_employees_count
FROM employee e
JOIN department d ON e.department = d.department_id
ORDER BY e.emp_id DESC;
