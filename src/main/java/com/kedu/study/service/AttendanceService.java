package com.kedu.study.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kedu.study.dao.AttendanceDAO;
import com.kedu.study.dto.ActivityDTO;
import com.kedu.study.dto.AttendanceDTO;

import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AttendanceService {

	@Autowired
	private AttendanceDAO ADao;
	
	public Timestamp getTodayCheckIn(String userId) {
	    return ADao.getTodayCheckIn(userId);
	}
	public Timestamp getTodayCheckOut(String userId) {
	    return ADao.getTodayCheckOut(userId);
	}


	public int checkIn(AttendanceDTO attendancedto) {
		LocalDateTime checkInTime = attendancedto.getCheck_in_time().toLocalDateTime();

		double workHours = 0;
		double overtimeHours = 0;

		attendancedto.setWork_hours(workHours);
		attendancedto.setOvertime_hours(overtimeHours);
		attendancedto.setRecord_date(Timestamp.valueOf(LocalDateTime.now()));
		return ADao.checkIn(attendancedto);
	}

	public int checkOut(AttendanceDTO attendancedto) {
		if (attendancedto.getCheck_out_time() == null) {
			attendancedto.setCheck_out_time(Timestamp.valueOf(LocalDateTime.now()));
		}

		Timestamp checkInTimestamp = ADao.getTodayCheckIn(attendancedto.getEmp_loginId());

		if (checkInTimestamp == null) {
			throw new IllegalArgumentException("출근 기록이 없습니다.");
		}

		attendancedto.setCheck_in_time(checkInTimestamp);

		LocalDateTime checkInTime = checkInTimestamp.toLocalDateTime();
		LocalDateTime checkOutTime = attendancedto.getCheck_out_time().toLocalDateTime();

		long workMinutes = ChronoUnit.MINUTES.between(checkInTime, checkOutTime);
		double workHours = workMinutes / 60.0;

		double overtimeHours = 0;
		if (workHours > 8) {
			overtimeHours = workHours - 8;
		}

		attendancedto.setWork_hours(workHours);
		attendancedto.setOvertime_hours(overtimeHours);

		int result = ADao.checkOut(attendancedto);
		if (result > 0) {
			System.out.println("퇴근 기록과 근무 시간이 DB에 성공적으로 업데이트되었습니다.");
		} else {
			System.out.println("퇴근 기록 업데이트 실패");
		}

		return result;
	}
	
	
	public Integer findTodayAttendanceId(String userId) {
	    return ADao.findTodayAttendanceId(userId);
	}

	public void handleActivityChange(ActivityDTO dto) {
	    // 외근 또는 업무인 경우에만 이전 활동 종료
	    if ("외근".equals(dto.getActivity_type()) || "업무".equals(dto.getActivity_type())) {
	    	ADao.endLastActivity(dto.getAttendance_id());
	    }

	    // 새 활동은 end_time 없이 시작
	    dto.setEnd_time(null);
	    dto.setActivity_hours(0.0);
	    ADao.insertActivity(dto);
	}


}
