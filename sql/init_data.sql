INSERT INTO users (username, password, real_name, role) VALUES ('admin', '123456', '管理员', 'admin');
INSERT INTO users (username, password, real_name, role) VALUES ('user01', '123456', '张三', 'user');
INSERT INTO users (username, password, real_name, role) VALUES ('user02', '123456', '李四', 'user');

INSERT INTO rooms (room_no, room_type, price, status, remark) VALUES ('101', '单人间', 188, '空闲', '普通单人间');
INSERT INTO rooms (room_no, room_type, price, status, remark) VALUES ('102', '单人间', 188, '空闲', '靠近电梯');
INSERT INTO rooms (room_no, room_type, price, status, remark) VALUES ('201', '双人间', 288, '空闲', '适合两人入住');
INSERT INTO rooms (room_no, room_type, price, status, remark) VALUES ('301', '豪华间', 388, '维修中', '设备维护');

INSERT INTO orders (user_id, room_id, start_date, end_date, total_price, status, create_time) VALUES (
    2,
    1,
    '2026-05-01',
    '2026-05-03',
    376,
    '待确认',
    NOW()
);

INSERT INTO orders (user_id, room_id, start_date, end_date, total_price, status, create_time) VALUES (
    3,
    2,
    '2026-04-20',
    '2026-04-22',
    376,
    '已完成',
    NOW() - INTERVAL 10 DAY
);

INSERT INTO orders (user_id, room_id, start_date, end_date, total_price, status, create_time) VALUES (
    2,
    3,
    '2026-05-10',
    '2026-05-12',
    576,
    '已确认',
    NOW() - INTERVAL 2 DAY
);

INSERT INTO orders (user_id, room_id, start_date, end_date, total_price, status, create_time) VALUES (
    3,
    1,
    '2026-03-01',
    '2026-03-02',
    188,
    '已取消',
    NOW() - INTERVAL 30 DAY
);
