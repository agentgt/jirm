SELECT 
c.id, c.name, c.tags, c.category, c.description, 
c.division, c.experience_level as "experienceLevel", 
c.locations, c.type, c.parent_id as "parentId", 
g.latitude as "latitude", g.longitude as "longitude"
FROM campaign c
LEFT OUTER JOIN 
	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg
	INNER JOIN geo geo on geo.id = cg.geo 
	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < now() -- {}
	) g on g.campaign = c.id
WHERE c.type = 'JOBPAGE' AND c.createts < now() -- {}
ORDER BY c.createts ASC, c.id, g.latitude, g.longitude
LIMIT 100 -- {}
OFFSET 1 -- {}
