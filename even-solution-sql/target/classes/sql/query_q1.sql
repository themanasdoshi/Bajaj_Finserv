SELECT d.department_name,
       e.first_name,
       e.last_name,
       SUM(p.amount) AS total_payment
FROM employee e
JOIN department d ON e.department = d.department_id
JOIN payments p ON e.emp_id = p.emp_id
GROUP BY d.department_name, e.emp_id, e.first_name, e.last_name
HAVING SUM(p.amount) = (
    SELECT MAX(total_amt)
    FROM (
        SELECT SUM(p2.amount) AS total_amt
        FROM employee e2
        JOIN payments p2 ON e2.emp_id = p2.emp_id
        WHERE e2.department = e.department
        GROUP BY e2.emp_id
    ) sub
)
ORDER BY d.department_name;
