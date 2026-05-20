const db = require('../config/db'); // Import kết nối database 

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

const getActiveQuarantines = async (cccd) => {
    const query = `
        SELECT 
            cl.MaCachLy,
            DATE_FORMAT(cl.NgayBatDau, '%d/%m/%Y') AS NgayBatDau,
            DATE_FORMAT(cl.NgayKetThuc, '%d/%m/%Y') AS NgayKetThuc,
            cl.DiaDiem AS DiaDiemChiTiet,
            csyt.TenCoSo AS TenCoSoYTe
        FROM CACHLY cl
        JOIN NGUOIDUNG nd ON cl.MaNguoiDung = nd.MaNguoiDung
        LEFT JOIN COSOYTE csyt ON cl.MaCoSo = csyt.MaCoSo
        WHERE nd.CCCD = ? AND CURDATE() BETWEEN cl.NgayBatDau AND cl.NgayKetThuc;
    `;
    
    // Thực thi câu truy vấn với tham số cccd
    const [rows] = await db.execute(query, [cccd]);
    return rows;
};

const getTestHistoryByCCCD = async (cccd) => {
    try {
        const query = `
            SELECT 
                n.HoTen,
                n.CCCD,
                x.MaXetNghiem,
                ROW_NUMBER() OVER(PARTITION BY x.MaNguoiDung ORDER BY x.NgayXetNghiem ASC) AS LanXetNghiem,
                x.LoaiXetNghiem,
                DATE_FORMAT(x.NgayXetNghiem, '%d/%m/%Y') AS NgayXetNghiem,
                x.KetQua, 
                c.TenCoSo AS DiaDiemXetNghiem
            FROM XETNGHIEM x
            JOIN NGUOIDUNG n ON x.MaNguoiDung = n.MaNguoiDung
            LEFT JOIN COSOYTE c ON x.MaCoSo = c.MaCoSo
            WHERE n.CCCD = ?
            ORDER BY x.NgayXetNghiem DESC;
        `;
        
        const [rows] = await db.execute(query, [cccd]);
        return rows;
    } catch (error) {
        throw error;
    }
};

module.exports = {
    getVaccineDosesByCCCD,
    getActiveQuarantines,
    getTestHistoryByCCCD
};