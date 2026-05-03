INSERT INTO users VALUES (seq_users.NEXTVAL, 'admin', '123456', '管理员', 'admin');
INSERT INTO users VALUES (seq_users.NEXTVAL, 'user01', '123456', '张三', 'user');
INSERT INTO users VALUES (seq_users.NEXTVAL, 'user02', '123456', '李四', 'user');

INSERT INTO rooms VALUES (seq_rooms.NEXTVAL, '101', '单人间', 188, '空闲', '普通单人间');
INSERT INTO rooms VALUES (seq_rooms.NEXTVAL, '102', '单人间', 188, '空闲', '靠近电梯');
INSERT INTO rooms VALUES (seq_rooms.NEXTVAL, '201', '双人间', 288, '空闲', '适合两人入住');
INSERT INTO rooms VALUES (seq_rooms.NEXTVAL, '301', '豪华间', 388, '维修中', '设备维护');

INSERT INTO orders (
    order_id, user_id, room_id, start_date, end_date, total_price, status, create_time
) VALUES (
    seq_orders.NEXTVAL,
    2,
    1,
    TO_DATE('2026-05-01', 'YYYY-MM-DD'),
    TO_DATE('2026-05-03', 'YYYY-MM-DD'),
    376,
    '待确认',
    SYSDATE
);

INSERT INTO orders (
    order_id, user_id, room_id, start_date, end_date, total_price, status, create_time
) VALUES (
    seq_orders.NEXTVAL,
    3,
    2,
    TO_DATE('2026-04-20', 'YYYY-MM-DD'),
    TO_DATE('2026-04-22', 'YYYY-MM-DD'),
    376,
    '已完成',
    SYSDATE - 10
);

INSERT INTO orders (
    order_id, user_id, room_id, start_date, end_date, total_price, status, create_time
) VALUES (
    seq_orders.NEXTVAL,
    2,
    3,
    TO_DATE('2026-05-10', 'YYYY-MM-DD'),
    TO_DATE('2026-05-12', 'YYYY-MM-DD'),
    576,
    '已确认',
    SYSDATE - 2
);

INSERT INTO orders (
    order_id, user_id, room_id, start_date, end_date, total_price, status, create_time
) VALUES (
    seq_orders.NEXTVAL,
    3,
    1,
    TO_DATE('2026-03-01', 'YYYY-MM-DD'),
    TO_DATE('2026-03-02', 'YYYY-MM-DD'),
    188,
    '已取消',
    SYSDATE - 30
);
