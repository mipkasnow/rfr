INSERT INTO friendship (id, requester_id, addressee_id, status, created_at, responded_at)
SELECT UUID_TO_BIN(UUID(), TRUE), requester.id, addressee.id, :status, NOW(6),
       IF(:status = 'ACCEPTED', NOW(6), NULL)
FROM `user` requester
JOIN `user` addressee ON addressee.username = :addressee
WHERE requester.username = :requester
