const pool = require('../config/db');
const jwt = require('jsonwebtoken');

const login = async (req, res) => {
    const { TenDangNhap, MatKhau } = req.body;

    // 1. Kiểm tra xem người dùng có gửi đủ dữ liệu không
    if (!TenDangNhap || !MatKhau) {
        return res.status(400).json({ message: 'Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!' });
    }

    try {
        // 2. Tìm tài khoản trong Database
        const [users] = await pool.query(
            'SELECT * FROM TAIKHOAN WHERE TenDangNhap = ?',
            [TenDangNhap]
        );

        if (users.length === 0) {
            return res.status(401).json({ message: 'Tài khoản không tồn tại!' });
        }

        const user = users[0];

        // 3. So sánh mật khẩu (KHÔNG MÃ HÓA, so sánh trực tiếp)
        if (MatKhau !== user.MatKhau) {
            return res.status(401).json({ message: 'Mật khẩu không chính xác!' });
        }

        // 4. Lấy thêm thông tin HoTen và CCCD từ bảng NGUOIDUNG để hiển thị trên App
        const [userDetails] = await pool.query(
            'SELECT HoTen, CCCD FROM NGUOIDUNG WHERE MaNguoiDung = ?',
            [user.MaNguoiDung]
        );
        const hoTen = userDetails.length > 0 ? userDetails[0].HoTen : 'Unknown';
        const cccd = userDetails.length > 0 ? userDetails[0].CCCD : ''; 

        // 5. Tạo Token (Thẻ thông hành phiên đăng nhập)
        const token = jwt.sign(
            {
                MaTaiKhoan: user.MaTaiKhoan,
                MaNguoiDung: user.MaNguoiDung,
                MaVaiTro: user.MaVaiTro,
                MaToChuc: user.MaToChuc
            },
            process.env.JWT_SECRET,
            { expiresIn: '1d' } 
        );

        // 6. Trả kết quả thành công về cho Client
        return res.status(200).json({
            message: 'Đăng nhập thành công!',
            token: token,
            data: {
                MaNguoiDung: user.MaNguoiDung,
                HoTen: hoTen,
                MaVaiTro: user.MaVaiTro,
                CCCD: cccd 
            }
        });

    } catch (error) {
        console.error('Lỗi khi đăng nhập:', error);
        return res.status(500).json({ message: 'Lỗi server nội bộ' });
    }
};

const register = async (req, res) => {
    // Nhận các trường dữ liệu từ Android gửi lên
    const { HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email, MatKhau } = req.body;

    // 1. Kiểm tra các trường bắt buộc để tránh crash DB
    if (!HoTen || !CCCD || !MatKhau) {
        return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin bắt buộc (Họ tên, CCCD, Mật khẩu)!' });
    }

    // Tên đăng nhập giống hệt CCCD theo yêu cầu
    const TenDangNhap = CCCD; 

    // Xin cấp một kết nối (connection) từ pool để chạy Transaction
    const connection = await pool.getConnection();

    try {
        // Bắt đầu Transaction (giao dịch)
        await connection.beginTransaction();

        // 2. Insert thông tin vào bảng NGUOIDUNG trước
        const queryNguoiDung = `
            INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        `;
        const [resultNguoiDung] = await connection.query(queryNguoiDung, [
            HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email
        ]);

        // Lấy MaNguoiDung tự động tăng vừa được sinh ra từ bảng NGUOIDUNG
        const maNguoiDungMoi = resultNguoiDung.insertId;

        // 3. Dùng MaNguoiDung đó để insert tiếp vào bảng TAIKHOAN (Lưu mật khẩu trực tiếp)
        const queryTaiKhoan = `
            INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, MaNguoiDung) 
            VALUES (?, ?, ?)
        `;
        await connection.query(queryTaiKhoan, [
            TenDangNhap, MatKhau, maNguoiDungMoi
        ]);

        // 4. Nếu cả 2 lệnh trên thành công, xác nhận lưu vĩnh viễn vào DB
        await connection.commit();

        return res.status(201).json({
            message: 'Đăng ký tài khoản thành công!',
            data: {
                TenDangNhap: TenDangNhap,
                HoTen: HoTen
            }
        });

    } catch (error) {
        // Nếu có bất kỳ lỗi nào xảy ra trong block `try`, hủy bỏ lệnh chèn của cả 2 bảng
        await connection.rollback();
        console.error('Lỗi khi đăng ký:', error);

        // Bắt lỗi trùng CCCD hoặc trùng Tên đăng nhập (Ràng buộc UNIQUE trong SQL)
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({ message: 'Tài khoản hoặc Số căn cước công dân đã tồn tại trên hệ thống!' });
        }

        return res.status(500).json({ message: 'Lỗi server nội bộ' });
    } finally {
        // Luôn luôn giải phóng kết nối trả lại cho pool để không bị nghẽn DB
        connection.release();
    }
};

// --- HÀM LẤY DỮ LIỆU TIÊM CHỦNG TỪ DATABASE CHUẨN ---
const getThongTinTiemChung = async (req, res) => {
    const cccd = req.params.cccd;

    try {
        // 1. Tìm MaNguoiDung dựa vào CCCD
        const [users] = await pool.query(
            'SELECT MaNguoiDung FROM NGUOIDUNG WHERE CCCD = ?',
            [cccd]
        );

        if (users.length === 0) {
            return res.status(404).json({ message: 'Không tìm thấy thông tin công dân!' });
        }

        const maNguoiDung = users[0].MaNguoiDung;

        // 2. Lấy chi tiết tiêm chủng từ bảng TIEMCHUNG và COSOYTE
        const queryTiemChung = `
            SELECT 
                TC.SoMui AS muiSo, 
                TC.LoaiVacXin AS tenVaccine, 
                CS.TenCoSo AS diaDiem, 
                DATE_FORMAT(TC.NgayTiem, '%d/%m/%Y') AS ngayTiem
            FROM TIEMCHUNG TC
            LEFT JOIN COSOYTE CS ON TC.MaCoSo = CS.MaCoSo
            WHERE TC.MaNguoiDung = ?
            ORDER BY TC.SoMui ASC
        `;
        
        const [danhSachTiem] = await pool.query(queryTiemChung, [maNguoiDung]);

        // 3. Tính toán màu thẻ tự động dựa trên số mũi tiêm đếm được
        const soMuiTiem = danhSachTiem.length;
        let loaiThe = 'DO'; // Mặc định Thẻ Đỏ
        
        if (soMuiTiem === 1) {
            loaiThe = 'VANG';
        } else if (soMuiTiem >= 2) {
            loaiThe = 'XANH';
        }

        // 4. Trả kết quả về cho Android đúng chuẩn
        return res.status(200).json({
            message: 'Lấy dữ liệu tiêm chủng thành công!',
            soMuiTiem: soMuiTiem,
            loaiThe: loaiThe,
            danhSachTiem: danhSachTiem
        });

    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu tiêm chủng:', error);
        return res.status(500).json({ message: 'Lỗi server nội bộ' });
    }
};

module.exports = {
    login,
    register,
    getThongTinTiemChung
};