const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
require('dotenv').config();
require('./config/db'); 

// THÊM DÒNG NÀY: Import file route
const authRoutes = require('./routes/authRoutes'); 

const app = express();
const PORT = process.env.PORT || 3000;

app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/api/health', (req, res) => {
    res.status(200).json({ 
        status: 'success', 
        message: 'Backend is running smoothly!' 
    });
});

// THÊM DÒNG NÀY: Kết nối route vào đường dẫn /api/auth
app.use('/api/auth', authRoutes);

app.listen(PORT, () => {
    console.log(`🚀 Server is running on port ${PORT}`);
});