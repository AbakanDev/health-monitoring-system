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

const getF0TrendThisYear = async () => {
    // Câu query đếm số ca dương tính theo từng tháng trong năm nay
    // Chú ý: Đổi chữ 'Dương tính' thành giá trị thực tế bạn lưu trong cột KetQua
    const query = `
        SELECT 
            MONTH(NgayXetNghiem) as month, 
            COUNT(MaXetNghiem) as total_cases 
        FROM XETNGHIEM 
        WHERE 
            YEAR(NgayXetNghiem) = YEAR(CURDATE()) 
            AND KetQua = 'Dương tính' 
        GROUP BY MONTH(NgayXetNghiem)
        ORDER BY month ASC;
    `;

    const [rows] = await db.execute(query);

    // Khởi tạo mảng 12 tháng với giá trị mặc định là 0
    const monthlyData = new Array(12).fill(0);

    // Đổ dữ liệu từ DB vào mảng dựa theo index (tháng 1 là index 0)
    rows.forEach(row => {
        const monthIndex = row.month - 1; // MySQL trả về tháng 1-12, mảng JS từ 0-11
        monthlyData[monthIndex] = row.total_cases;
    });

    return monthlyData;
};

const getVaccinationRates = async () => {
    const query = `
        SELECT 
            ROUND(
                COUNT(DISTINCT CASE WHEN T.SoMui >= 1 THEN T.MaNguoiDung END) * 100.0 
                / COUNT(DISTINCT N.MaNguoiDung), 2
            ) AS TyLeMui1,
            
            ROUND(
                COUNT(DISTINCT CASE WHEN T.SoMui >= 2 THEN T.MaNguoiDung END) * 100.0 
                / COUNT(DISTINCT N.MaNguoiDung), 2
            ) AS TyLeMui2
        FROM NGUOIDUNG N
        LEFT JOIN TIEMCHUNG T ON N.MaNguoiDung = T.MaNguoiDung;
    `;

    const [rows] = await db.execute(query);
    
    // Vì kết quả trả về chỉ có 1 dòng chứa TyLeMui1 và TyLeMui2, ta lấy phần tử đầu tiên
    return rows[0]; 
};

const getDashboardStats = async () => {
    try {
        const queryF0 = `
            SELECT COUNT(*) AS SoCaF0
            FROM (
                SELECT MaNguoiDung, KetQua, 
                       ROW_NUMBER() OVER (PARTITION BY MaNguoiDung ORDER BY NgayXetNghiem DESC) as rn
                FROM XETNGHIEM
            ) tmp
            WHERE rn = 1 AND KetQua = N'Dương tính';
        `;

        const queryF1F2 = `
            SELECT COUNT(DISTINCT MaNguoiDung) AS SoCaF1F2
            FROM GHINHANCAPDO 
            WHERE MaCapDo IN ('F1', 'F2')
              AND MaNguoiDung NOT IN (
                  SELECT MaNguoiDung
                  FROM (
                      SELECT MaNguoiDung, KetQua, 
                             ROW_NUMBER() OVER (PARTITION BY MaNguoiDung ORDER BY NgayXetNghiem DESC) as rn
                      FROM XETNGHIEM
                  ) tmp
                  WHERE rn = 1 AND KetQua = N'Dương tính'
              );
        `;

        const queryCachLy = `
            SELECT COUNT(DISTINCT MaNguoiDung) AS SoNguoiCachLy
            FROM CACHLY
            WHERE NgayBatDau <= CURDATE() 
              AND (NgayKetThuc IS NULL OR NgayKetThuc >= CURDATE());
        `;
        
        const queryVungDo = `
            SELECT COUNT(*) AS SoVungDo
            FROM KHUVUC
            WHERE Capdovung = 2;
        `;

        const [[f0Result], [f1f2Result], [cachLyResult], [vungDoResult]] = await Promise.all([
            db.execute(queryF0),
            db.execute(queryF1F2),
            db.execute(queryCachLy),
            db.execute(queryVungDo)
        ]);

        return {
            SoCaF0: f0Result[0].SoCaF0 || 0,
            SoCaF1F2: f1f2Result[0].SoCaF1F2 || 0,
            SoNguoiCachLy: cachLyResult[0].SoNguoiCachLy || 0,
            SoVungDo: vungDoResult[0].SoVungDo || 0
        };
    } catch (error) {
        throw error;
    }
};

const getFirstTestByCCCD = async (cccd) => {
    try {
        const query = `
            SELECT 
                nd.MaNguoiDung,
                nd.HoTen,
                nd.CCCD,
                xn.LoaiXetNghiem,
                DATE_FORMAT(xn.NgayXetNghiem, '%d/%m/%Y') AS NgayXetNghiem,
                xn.KetQua
            FROM NGUOIDUNG nd
            JOIN XETNGHIEM xn ON nd.MaNguoiDung = xn.MaNguoiDung
            WHERE nd.CCCD = ?
            ORDER BY xn.NgayXetNghiem DESC
            LIMIT 1;
        `;
        
        const [rows] = await db.execute(query, [cccd]);
        return rows[0]; // Trả về object đầu tiên (hoặc undefined nếu người này chưa từng xét nghiệm)
    } catch (error) {
        throw error;
    }
};

module.exports = {
    getVaccineDosesByCCCD,
    getActiveQuarantines,
    getTestHistoryByCCCD,
    getF0TrendThisYear,
    getVaccinationRates,
    getDashboardStats,
    getFirstTestByCCCD
};