const healthService = require('../services/healthService');

const getVaccineInfo = async (req, res) => {
    try {
        const cccd = req.params.cccd; // Lấy CCCD từ URL

        if (!cccd) {
            return res.status(400).json({ message: 'Vui lòng cung cấp CCCD' });
        }

        const data = await healthService.getVaccineDosesByCCCD(cccd);

        if (data.length === 0) {
            return res.status(404).json({ message: 'Không tìm thấy dữ liệu tiêm chủng cho CCCD này' });
        }

        return res.status(200).json({
            message: 'Lấy dữ liệu thành công',
            data: data
        });

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Lỗi server khi lấy dữ liệu tiêm chủng' });
    }
};


const getQuarantineStatus = async (req, res) => {
    try {
        const cccd = req.params.cccd;

        if (!cccd) {
            return res.status(400).json({ 
                success: false, 
                message: 'Vui lòng cung cấp CCCD' 
            });
        }

        const quarantineList = await healthService.getActiveQuarantines(cccd);
        
        // Nếu mảng có phần tử tức là đang trong thời gian cách ly
        const isQuarantined = quarantineList.length > 0;

        return res.status(200).json({
            success: true,
            message: isQuarantined ? 'Công dân đang trong diện cách ly' : 'Công dân không bị cách ly',
            isQuarantined: isQuarantined,
            data: quarantineList // Chứa thông tin ngày bắt đầu, kết thúc, địa điểm (nếu có)
        });
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu cách ly:', error);
        return res.status(500).json({
            success: false,
            message: 'Đã xảy ra lỗi trên server khi lấy dữ liệu cách ly',
            error: error.message
        });
    }
};

module.exports = {
    getVaccineInfo,
    getQuarantineStatus
};