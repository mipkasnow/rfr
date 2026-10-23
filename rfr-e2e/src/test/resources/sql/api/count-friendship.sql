SELECT COUNT(*) FROM friendship
WHERE requester_id = (SELECT id FROM `user` WHERE username = :requester)
  AND addressee_id = (SELECT id FROM `user` WHERE username = :addressee)
  AND status = :status
