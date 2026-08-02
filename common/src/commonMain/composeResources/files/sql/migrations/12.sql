UPDATE checkin_queue SET message="checkin_closed" WHERE message="checkin closed";
UPDATE checkin_queue SET message="not_approved" WHERE message="booking not approved yet";
UPDATE checkin_queue SET message="already_checked_in" WHERE message="already checked in";
