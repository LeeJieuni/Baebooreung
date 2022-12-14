package com.pro.baebooreung.businessservice.service;

import com.pro.baebooreung.businessservice.client.GpsServiceClient;
import com.pro.baebooreung.businessservice.client.UserServiceClient;
import com.pro.baebooreung.businessservice.domain.*;
import com.pro.baebooreung.businessservice.domain.repository.DeliveryRepository;
import com.pro.baebooreung.businessservice.domain.repository.OrderRepository;
import com.pro.baebooreung.businessservice.domain.repository.RouteRepository;
import com.pro.baebooreung.businessservice.dto.*;
import com.pro.baebooreung.businessservice.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class RouteServiceImpl implements RouteService {
    RouteRepository routeRepository;
    DeliveryRepository deliveryRepository;
    UserServiceClient userServiceClient;
    GpsServiceClient gpsServiceClient;
    NavigationService navigationService;

    DeliveryService deliveryService;

    OrderRepository orderRepository;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository, EntityManager em,DeliveryRepository deliveryRepository, UserServiceClient userServiceClient, GpsServiceClient gpsServiceClient, NavigationService navigationService, DeliveryService deliveryService, OrderRepository orderRepository){
        this.routeRepository = routeRepository;
        this.deliveryRepository = deliveryRepository;
        this.em = em;
        this.userServiceClient = userServiceClient;
        this.gpsServiceClient = gpsServiceClient;
        this.navigationService = navigationService;
        this.deliveryService = deliveryService;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<ResponseRoute> getRouteByUser(int userId){//}, Data date){
        Iterable<Route> routeEntityList = routeRepository.findByUserId(userId);// userId로 route 리스트 찾기
        List<ResponseRoute> responseRoutes = new ArrayList<>();

        routeEntityList.forEach(route -> {

            ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            ResponseRoute r = mapper.map(route,ResponseRoute.class);
            responseRoutes.add(r);

        });
        return responseRoutes;
    }

    @Override
    public ResponseRoute getRoute(int routeId) {
        Optional<Route> routeEntity = routeRepository.findById(routeId);
        ResponseRoute responseRoute = new ResponseRoute();

//        List<Route> nameList = Optional.ofNullable(getNames()).orElseGet(() -> new ArrayList<>());
        if(routeEntity.isPresent()){
            ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            responseRoute = mapper.map(routeEntity,ResponseRoute.class);
        }
        for(int i=0;i<responseRoute.getDeliveryList().size();i++){
            ResponseDelivery delivery = responseRoute.getDeliveryList().get(i);
            List<Order> orderList = orderRepository.findByDeliveryId(delivery.getId());
            if(orderList == null || orderList.size() == 0){
                delivery.setOrderNum(0);
            } else {
                delivery.setOrderNum(orderList.size());
            }
        }

        for(int i=0;i<responseRoute.getDeliveryList().size();i++){
            ResponseDelivery delivery = responseRoute.getDeliveryList().get(i);
            List<Order> orderList = orderRepository.findByDropId(delivery.getId());
            if(orderList != null && orderList.size() != 0){
                delivery.setOrderNum(orderList.size());
            }
        }
        return responseRoute;
    }

    @Override
    public List<ResponseRoute> getRouteByUserNDate(int userId){//}, Data date){
        //드라이버의 해당하는 날짜(?or 오늘???)의 done이 아닌 루트들
        LocalDate today =LocalDate.now();
        Iterable<Route> routeEntityList = routeRepository.findByUserIdAndDeliveryDateTime(userId,today);

        List<ResponseRoute> responseRoutes = new ArrayList<>();
        routeEntityList.forEach(route -> {

            if(!route.isDone()){ //완료하지 않았다면
                ModelMapper mapper = new ModelMapper();
                mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
                ResponseRoute r = mapper.map(route,ResponseRoute.class);
                responseRoutes.add(r);
            }

        });

        return responseRoutes;
    }

//    @Transactional
    @Override
    public RouteDto startWork(int userId, int routeId){
        Optional<Delivery> findDelivery = deliveryRepository.findByRouteIdAndSequence(routeId,1);
        Optional<Route> findRoute = routeRepository.findById(routeId);
        /* feign client */
        RequestStart requestStart = new RequestStart();
        if(findDelivery.isPresent()){
            requestStart = new RequestStart(userId,routeId,findDelivery.get().getId());
            log.info(">>>>>>>>"+requestStart.toString());
            ResponseUser responseUser = userServiceClient.startWork(requestStart);
        }else{
            log.info(">>>>>>>> There's no delivery");
        }

        LocalTime now = LocalTime.now();
        findRoute.get().setActualStartTime(now);
        routeRepository.save(findRoute.get());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        RouteDto response = mapper.map(findRoute.get(), RouteDto.class);
        //반환

        return response;
    }

//    public void checkIn(int userId,int routeId,int sequence){ //,int deliveryId
//        // userId를 받아서 그 사람의 userId가 gps에서 어떤 위치에 있는지 반환받아서   - 2순위
//        // routeId 로 찾은 route의 delivery들 중에 (아마도 순서대로) 거리 내에 있고  - 1순위
//        // 머무는 시간이 좀 걸린다면 체크인+도착시간
//        // + user의 route_id,delivery_id값 다음 목적지로 바꾸기(마지막일때 0이나 null로)
//
//
//        //체크인 여부를 판단하는 기준
//        //- 음식점 주위에 이 사람이 지나치는지?
//        //- 약간 여기 좀 더 머무르는지?
//        //- 사진?
//        //- 체크인 모호한 경우 - 사진 찍도록 유도
//
//        ResponseGps findUserGps = gpsServiceClient.getGps(userId);
//
//        double user_lat = Double.valueOf(findUserGps.getLatitude());
//        double user_long = Double.valueOf(findUserGps.getLongitude());
//
//        Optional<Delivery> findDelivery = deliveryRepository.findByRouteIdAndSequence(routeId,sequence);
//        double del_lat = findDelivery.get().getLatitude();
//        double del_long = findDelivery.get().getLongitude();
//
//        double diff_lat = del_lat - user_lat;
//        double diff_long = del_long - user_long;
//
//        double distance = Math.sqrt(diff_lat*diff_lat + diff_long*diff_long);
//        ////////////
//
//        findDelivery.get().updateDelActualTime(LocalTime.now());
//        deliveryRepository.save(findDelivery.get());
//
//        // user에서 넣어주기 feign client 코드 작성
//        userServiceClient.checkIn(new RequestCheckIn(userId,findDelivery.get().getId()));

//        //if 끝이라면 work_status와 route_id,delivery_id 비어주기 (그럼 user에서 delivery_id만 넣어줘도 될듯..)
//        Optional<Route> findRoute = routeRepository.findById(routeId);
//        int listSize = findRoute.get().getDeliveryList().size();
//        if(listSize == sequence){
//            userServiceClient.checkIn(new RequestCheckIn(userId, 0,0));
//            findRoute.get().updateDone(true);
//            routeRepository.save(findRoute.get());
//        }
//    }

    public EndWorkDto endWork(int userId,int routeId){ //,int deliveryId
        //if 끝이라면 work_status와 route_id,delivery_id 비어주기 (그럼 user에서 delivery_id만 넣어줘도 될듯..)
        Optional<Route> findRoute = routeRepository.findById(routeId);
        if(findRoute.isPresent()){
            findRoute.get().updateDone(true);
            routeRepository.save(findRoute.get());
            userServiceClient.endWork(userId);

            // 늦은 시각이 있는지 확인
            boolean late = false;
            List<Delivery> deliveryList = findRoute.get().getDeliveryList();
            for (Delivery d:deliveryList) {
                if(d.getDelActualTime().isAfter(d.getDelScheduledTime())){
                    late = true;
                    break;
                }
            }

            EndWorkDto endWorkDto = new EndWorkDto(routeId,late);
            return endWorkDto;

        }else{
            throw new NullPointerException();
        }


    }

    // 체크인 처리
    public CheckinResponseDto checkIn(int userId, CheckInDto checkInDto) throws Exception {
        Optional<Delivery> findDelivery = deliveryRepository.findById(checkInDto.getDeliveryId());
        if(findDelivery.isPresent()){
            findDelivery.get().checkIn(true, checkInDto.getImg());
            deliveryRepository.save(findDelivery.get());
            Optional<Delivery> nextDelivery = deliveryRepository.findByRouteIdAndSequence(findDelivery.get().getRoute().getId(), findDelivery.get().getSequence()+1);
            if(nextDelivery.isPresent()){
                // user에서 넣어주기 feign client 코드 작성
//                userServiceClient.checkIn(new RequestCheckIn(userId,nextDelivery.get().getId()));
                userServiceClient.checkIn(new RequestCheckIn(userId,checkInDto.getDeliveryId()+1));
                return CheckinResponseDto.builder().deliveryId(nextDelivery.get().getId()).build();
            } else {
                return CheckinResponseDto.builder().deliveryId(-1).build();
            }
        } else {
            throw new IllegalStateException("해당 deliveryID가 존재하지 않습니다.");
        }
    }

    @Override
    public List<RouteByRegionAndDateDto> getRouteByRegionAndDate(Region region, LocalDate localDate) throws Exception {
        Iterable<Route> findRouteList = routeRepository.findByRegionAndDate(region, localDate);
        List<RouteByRegionAndDateDto> list = new ArrayList<>();
        HashMap<String, Integer> univ = new HashMap<>();
        findRouteList.forEach(route -> {
            log.info("getRouteByRegionAndDate 내에 route: {}", route);
            String univName = route.getRouteName();
            if(univ.containsKey(route.getRouteName())){
                int i = Integer.parseInt(univ.get(univName).toString());
                char c = (char)i;
                univ.put(univName, univ.get(univName)+1);
                univName += String.valueOf(c);
            } else {
                univ.put(univName, 66);
                char c = (char)65;
                univName += String.valueOf(c);
            }
            RouteByRegionAndDateDto regionAndDateDto = RouteByRegionAndDateDto.builder().routeId(route.getId()).userId(route.getUserId()).routeName(univName)
                    .routeType(route.getRouteType()).done(route.isDone()).build();
            List<DeliveryDto> deliveryDtoList = new ArrayList<>();
            try {
                deliveryDtoList = deliveryService.getDeliveryList(route.getId());
                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDeliveryId(delivery.getId());
                    if(orderList == null || orderList.size() == 0){
                        delivery.setOrderNum(0);
                    } else {
                        delivery.setOrderNum(orderList.size());
                    }
                }

                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDropId(delivery.getId());
                    if(orderList != null && orderList.size() != 0){
                        delivery.setOrderNum(orderList.size());
                    }
                }


                regionAndDateDto.setDeliveryDtoList(deliveryDtoList);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            try {
                List<NavigationDto> navigationDtoList = navigationService.getNavigationGpsByRouteId(route.getId());
                if(navigationDtoList != null && navigationDtoList.size() != 0){
                    regionAndDateDto.setNavigationList(navigationDtoList);
                }
            } catch (Exception e){
            }
            list.add(regionAndDateDto);
        });

        return list;
    }

    @Override
    public List<RouteByRegionAndDateDto> getRouteByRegionAndDateAndRouteName(Region region, LocalDate localDate, String routeName) throws Exception {
        Iterable<Route> findRouteList = routeRepository.findByRegionAndDateAndRouteName(region, localDate, routeName);
        List<RouteByRegionAndDateDto> list = new ArrayList<>();
        HashMap<String, Integer> univ = new HashMap<>();
        findRouteList.forEach(route -> {
            String univName = route.getRouteName();
            if(univ.containsKey(route.getRouteName())){
                int i = Integer.parseInt(univ.get(univName).toString());
                char c = (char)i;
                univ.put(univName, univ.get(univName)+1);
                univName += String.valueOf(c);
            } else {
                univ.put(univName, 66);
                char c = (char)65;
                univName += String.valueOf(c);
            }
            RouteByRegionAndDateDto regionAndDateDto = RouteByRegionAndDateDto.builder().routeId(route.getId()).userId(route.getUserId()).routeName(univName)
                    .routeType(route.getRouteType()).done(route.isDone()).build();

            List<DeliveryDto> deliveryDtoList = new ArrayList<>();
            try {
                deliveryDtoList = deliveryService.getDeliveryList(route.getId());
                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDeliveryId(delivery.getId());
                    if(orderList == null || orderList.size() == 0){
                        delivery.setOrderNum(0);
                    } else {
                        delivery.setOrderNum(orderList.size());
                    }
                }

                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDropId(delivery.getId());
                    if(orderList != null && orderList.size() != 0){
                        delivery.setOrderNum(orderList.size());
                    }
                }

                regionAndDateDto.setDeliveryDtoList(deliveryDtoList);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            try {
                List<NavigationDto> navigationDtoList = navigationService.getNavigationGpsByRouteId(route.getId());
                if(navigationDtoList != null && navigationDtoList.size() != 0){
                    regionAndDateDto.setNavigationList(navigationDtoList);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            list.add(regionAndDateDto);
        });
        return list;
    }

    @Override
    public DriverRouteAndDeliveryDto getDriverRouteAndDelivery(int user_id) throws Exception {
        ResponseDriverRoute responseDriverRoute = userServiceClient.checkRouteAndDelivery(user_id);
        if(responseDriverRoute.isDrive()){
            // 전체 해당 루트에 포함된 delivery 지점 총 갯수
            List<Delivery> deliveryList = deliveryRepository.findByRouteId(responseDriverRoute.getRoute_id());
            // 현재 향하고 있는 delivery의 sequence 찾기
            Optional<Delivery> findDelivery = deliveryRepository.findById(responseDriverRoute.getDelivery_id());
            if(deliveryList == null || deliveryList.size() == 0) return DriverRouteAndDeliveryDto.builder().route_id(0).delivery_id(0).sequence(0).all_delvery(0).build();
            if(findDelivery.isPresent()){
                return DriverRouteAndDeliveryDto.builder().route_id(responseDriverRoute.getRoute_id()).delivery_id(responseDriverRoute.getDelivery_id())
                        .sequence(findDelivery.get().getSequence()).all_delvery(deliveryList.size()).build();
            } else {
                throw new Exception("DB 저장 이상");
            }
        } else {
            throw new IllegalStateException("현재 운행중인 드라이버가 아닙니다.");
        }
    }


    public String getDeliveryName(int deliveryId) throws Exception {
        Optional<Delivery> findDelivery = deliveryRepository.findById(deliveryId);
        if(findDelivery.isPresent()){
            return findDelivery.get().getDelName();
        }else{
            throw new Exception("id : "+deliveryId+ " 를 가진 목적지가 없습니다.");
        }
    }

    public LocalTime getRouteActualStartTime(int routeId) throws Exception {
        Optional<Route> findRoute = routeRepository.findById(routeId);
        if(findRoute.isPresent()){
            return findRoute.get().getActualStartTime();
        }else{
            throw new Exception("id : "+routeId+ " 를 가진 경로가 없습니다.");
        }
    }
    @Override
    public List<RouteByRegionAndDateDto> getRouteByRegionAndDateAndRouteNameAndRouteType(Region region, LocalDate localDate, String routeName, RouteType routeType) throws Exception {
        Iterable<Route> findRouteList = routeRepository.findByRegionAndDateAndRouteNameAndRouteType(region, localDate, routeName, routeType);
        List<RouteByRegionAndDateDto> list = new ArrayList<>();
        HashMap<String, Integer> univ = new HashMap<>();
        findRouteList.forEach(route -> {
            String univName = route.getRouteName();
            if(univ.containsKey(route.getRouteName())){
                int i = Integer.parseInt(univ.get(univName).toString());
                char c = (char)i;
                univ.put(univName, univ.get(univName)+1);
                univName += String.valueOf(c);
            } else {
                univ.put(univName, 66);
                char c = (char)65;
                univName += String.valueOf(c);
            }

            RouteByRegionAndDateDto regionAndDateDto = RouteByRegionAndDateDto.builder().routeId(route.getId()).userId(route.getUserId()).routeName(univName)
                    .routeType(route.getRouteType()).done(route.isDone()).build();

            List<DeliveryDto> deliveryDtoList = new ArrayList<>();
            try {
                deliveryDtoList = deliveryService.getDeliveryList(route.getId());
                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDeliveryId(delivery.getId());
                    if(orderList == null || orderList.size() == 0){
                        delivery.setOrderNum(0);
                    } else {
                        delivery.setOrderNum(orderList.size());
                    }
                }

                for(int i=0;i<deliveryDtoList.size();i++){
                    DeliveryDto delivery = deliveryDtoList.get(i);
                    List<Order> orderList = orderRepository.findByDropId(delivery.getId());
                    if(orderList != null && orderList.size() != 0){
                        delivery.setOrderNum(orderList.size());
                    }
                }

                regionAndDateDto.setDeliveryDtoList(deliveryDtoList);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            try {
                List<NavigationDto> navigationDtoList = navigationService.getNavigationGpsByRouteId(route.getId());
                if(navigationDtoList != null && navigationDtoList.size() != 0){
                    regionAndDateDto.setNavigationList(navigationDtoList);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            list.add(regionAndDateDto);
        });
        return list;
    }


}
