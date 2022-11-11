import {createSlice} from '@reduxjs/toolkit';

export const gpsSlice = createSlice({
  name: 'gps',
  initialState: {
    lat: '',
    lng: '',
  },
  reducers: {
    setGps: (state, action) => {
      state.lat = action.payload.lat;
      state.lng = action.payload.lng;
    },
  },
});

export const {setGps} = gpsSlice.actions;
export const lat = state => state.lat;
export const lng = state => state.lng;
export default gpsSlice.reducer;
