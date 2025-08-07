CREATE TABLE military_info (
                               member_id BIGINT NOT NULL,
                               mili_status VARCHAR(20),
                               mili_type VARCHAR(20),
                               specialty BIGINT,
                               unit BIGINT,
                               start_date DATE,
                               private_date DATE,
                               corporal_date DATE,
                               sergeant_date DATE,
                               discharge_date DATE,

                               PRIMARY KEY (member_id),

                               CONSTRAINT fk_militaryinfo_member
                                   FOREIGN KEY (member_id) REFERENCES member(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_militaryinfo_specialty
                                   FOREIGN KEY (specialty) REFERENCES specialty(id),

                               CONSTRAINT fk_militaryinfo_unit
                                   FOREIGN KEY (unit) REFERENCES unit(id)
);