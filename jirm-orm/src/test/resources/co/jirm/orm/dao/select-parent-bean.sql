SELECT parent_bean.id AS "id", 
test.string_prop AS "test.stringProp", 
test.long_prop AS "test.longProp",
test.timets AS "test.timeTS"
FROM parent_bean 
INNER JOIN test_bean test ON test.string_prop = parent_bean.test 
WHERE test.string_prop = 'test' -- {testName}
AND test.long_prop = 100 -- {testAmount}