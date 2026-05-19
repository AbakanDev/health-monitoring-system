const db = require('../config/db'); // Import kết nối database của bạn

const getVaccineDosesByCCCD = async (cccd) => {
    try {
        // Query kết hợp (JOIN) bảng NGUOIDUNG và TIEMCHUNG
        const query = `
            SELECT t.SoMui, t.LoaiVacXin, t.NgayTiem, n.HoTen
            FROM TIEMCHUNG t
            JOIN NGUOIDUNG n ON t.MaNguoiDung = n.MaNguoiDung
            WHERE n.CCCD = ?
            ORDER BY t.NgayTiem DESC
        `;
        
        const [rows] = await db.execute(query, [cccd]);
        return rows; 
    } catch (error) {
        throw error; // Ném lỗi ra để Controller bắt
    }
};

module.exports = {
    getVaccineDosesByCCCD
};