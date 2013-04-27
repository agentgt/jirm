-- {#ref}
SELECT
-- {> partial-test.sql#stuff}
c.id, c.name, c.tags, c.category, c.description
-- {<}
FROM campaign c
LEFT OUTER JOIN 
	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg
	INNER JOIN geo geo on geo.id = cg.geo 
	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < now() -- {}
	) g on g.campaign = c.id
ORDER BY c.createts ASC, c.id, g.latitude, g.longitude
LIMIT 100 -- {}
OFFSET 1 -- {}
-- {/ref}
