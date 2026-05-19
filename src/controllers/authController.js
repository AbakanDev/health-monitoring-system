const pool = require('../config/db');
const jwt = require('jsonwebtoken');

const login = async (req, res) => {
    const { TenDangNhap, MatKhau } = req.body;

    if (!TenDangNhap || !MatKhau) {
        return res.status(400).json({ message: 'Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!' });
    }

    try {
        const [users] = await pool.query(
            'SELECT * FROM TAIKHOAN WHERE TenDangNhap = ?',
            [TenDangNhap]
        );

        if (users.length === 0) {
            return res.status(401).json({ message: 'Tài khoản không tồn tại!' });
        }

        const user = users[0];

        if (MatKhau !== user.MatKhau) {
            return res.status(401).json({ message: 'Mật khẩu không chính xác!' });
        }

        // Lấy thêm HoTen và CCCD từ bảng NGUOIDUNG
        const [userDetails] = await pool.query(
            'SELECT HoTen, CCCD FROM NGUOIDUNG WHERE MaNguoiDung = ?',
            [user.MaNguoiDung]
        );
        const hoTen = userDetails.length > 0 ? userDetails[0].HoTen : 'Unknown';
        const cccd = userDetails.length > 0 ? userDetails[0].CCCD : ''; 

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
    const { HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email, MatKhau } = req.body;

    if (!HoTen || !CCCD || !MatKhau) {
        return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin bắt buộc (Họ tên, CCCD, Mật khẩu)!' });
    }

    const TenDangNhap = CCCD; 
    const connection = await pool.getConnection();

    try {
        await connection.beginTransaction();

        const queryNguoiDung = `
            INSERT INTO NGUOIDUNG (HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        `;
        const [resultNguoiDung] = await connection.query(queryNguoiDung, [
            HoTen, CCCD, NgaySinh, GioiTinh, SDT, DiaChi, Email
        ]);

        const maNguoiDungMoi = resultNguoiDung.insertId;

        const queryTaiKhoan = `
            INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, MaNguoiDung) 
            VALUES (?, ?, ?)
        `;
        await connection.query(queryTaiKhoan, [
            TenDangNhap, MatKhau, maNguoiDungMoi
        ]);

        await connection.commit();

        return res.status(201).json({
            message: 'Đăng ký tài khoản thành công!',
            data: {
                TenDangNhap: TenDangNhap,
                HoTen: HoTen
            }
        });

    } catch (error) {
        await connection.rollback();
        console.error('Lỗi khi đăng ký:', error);

        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({ message: 'Tài khoản hoặc Số căn cước công dân đã tồn tại trên hệ thống!' });
        }

        return res.status(500).json({ message: 'Lỗi server nội bộ' });
    } finally {
        connection.release();
    }
};

// --- HÀM MỚI: XỬ LÝ LẤY DỮ LIỆU TIÊM CHỦNG ---
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

        // 2. Lấy lịch sử tiêm chủng
        const queryTiemChung = `
            SELECT MuiSo AS muiSo, TenVaccine AS tenVaccine, DiaDiem AS diaDiem, NgayTiem AS ngayTiem
            FROM LICHSUTIEMCHUNG
            WHERE MaNguoiDung = ?
            ORDER BY MuiSo ASC
        `;
        const [danhSachTiem] = await pool.query(queryTiemChung, [maNguoiDung]);

        // 3. Tính toán màu thẻ
        const soMuiTiem = danhSachTiem.length;
        let loaiThe = 'DO'; // Mặc định Thẻ Đỏ
        
        if (soMuiTiem === 1) {
            loaiThe = 'VANG';
        } else if (soMuiTiem >= 2) {
            loaiThe = 'XANH';
        }

        // 4. Trả kết quả về cho Android
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
    getThongTinTiemChung // Đảm bảo đã export hàm này
};