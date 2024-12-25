-- Dữ liệu mẫu cho bảng nv (nhân viên)
INSERT INTO `nv` (`id`, `name`, `position`) VALUES
(1, 'Nguyen Van A', 'Manager'),
(2, 'Tran Thi B', 'Staff'),
(3, 'Le Van C', 'Supervisor');

-- Dữ liệu mẫu cho bảng kh (khách hàng)
INSERT INTO `kh` (`khid`, `address`, `id`, `name`, `phone`) VALUES
(1, '123 Main Street', 1, 'Pham Van D', '0987654321'),
(2, '456 Oak Avenue', 2, 'Hoang Thi E', '0123456789'),
(3, '789 Pine Road', 3, 'Nguyen Van F', '0912345678'),
(4, '101 Maple Lane', NULL, 'Le Thi G', '0999888777'); -- NULL ở `id` biểu thị khách hàng không liên kết với nhân viên.

-- Dữ liệu mẫu cho bảng hd (hợp đồng)
INSERT INTO `hd` (`hd_id`, `amount`, `contract_date`, `kh_id`) VALUES
(1, 5000, '2023-12-01', 1),
(2, 10000, '2023-12-15', 2),
(3, 7500, '2023-12-20', 3);
