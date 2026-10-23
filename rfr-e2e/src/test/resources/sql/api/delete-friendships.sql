DELETE FROM friendship WHERE UNHEX(:profile) IN (requester_id, addressee_id) AND status IN (:statuses)
