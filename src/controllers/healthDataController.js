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

module.exports = {
    getVaccineInfo
};