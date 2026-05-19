const jwt = require('jsonwebtoken');

const verifyToken = (req, res, next) => {
    // Lấy token từ header 'Authorization: Bearer <token>'
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ message: 'Không tìm thấy token xác thực!' });
    }

    try {
        // Giải mã token (nhớ thay 'SECRET_KEY' bằng key thực tế trong file .env của bạn)
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'SECRET_KEY');
        
        // Lưu thông tin user vào request để các tầng sau có thể dùng nếu cần
        req.user = decoded; 
        next(); // Cho phép đi tiếp vào Controller
    } catch (error) {
        return res.status(403).json({ message: 'Token không hợp lệ hoặc đã hết hạn!' });
    }
};

module.exports = { verifyToken };