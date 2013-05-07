SELECT
-- {#stuff}
c.id, c.name, c.tags, c.category, c.description, 
c.division, c.experience_level as "experienceLevel", 
c.locations, c.type, c.parent_id as "parentId", 
g.latitude as "latitude", g.longitude as "longitude"
-- {/stuff}
FROM campaign c
WHERE 
-- {> partial-test.sql#blah}
c.type = 'JOBPAGE' AND c.createts < now() -- {}
-- {<}
ORDER BY c.createts
LIMIT 100 -- {}
OFFSET 1 -- {}