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
        Iterable<Route> routeEntityList = routeRepository.findByUserId(userId);// userId??? route ????????? ??????
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
        //??????????????? ???????????? ??????(?or ?????????)??? done??? ?????? ?????????
        LocalDate today =LocalDate.now();
        Iterable<Route> routeEntityList = routeRepository.findByUserIdAndDeliveryDateTime(userId,today);

        List<ResponseRoute> responseRoutes = new ArrayList<>();
        routeEntityList.forEach(route -> {

            if(!route.isDone()){ //???????????? ????????????
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
        //??????

        return response;
    }

//    public void checkIn(int userId,int routeId,int sequence){ //,int deliveryId
//        // userId??? ????????? ??? ????????? userId??? gps?????? ?????? ????????? ????????? ???????????????   - 2??????
//        // routeId ??? ?????? route??? delivery??? ?????? (????????? ????????????) ?????? ?????? ??????  - 1??????
//        // ????????? ????????? ??? ???????????? ?????????+????????????
//        // + user??? route_id,delivery_id??? ?????? ???????????? ?????????(??????????????? 0?????? null???)
//
//
//        //????????? ????????? ???????????? ??????
//        //- ????????? ????????? ??? ????????? ????????????????
//        //- ?????? ?????? ??? ??? ????????????????
//        //- ???????
//        //- ????????? ????????? ?????? - ?????? ????????? ??????
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
//        // user?????? ???????????? feign client ?????? ??????
//        userServiceClient.checkIn(new RequestCheckIn(userId,findDelivery.get().getId()));

//        //if ???????????? work_status??? route_id,delivery_id ???????????? (?????? user?????? delivery_id??? ???????????? ??????..)
//        Optional<Route> findRoute = routeRepository.findById(routeId);
//        int listSize = findRoute.get().getDeliveryList().size();
//        if(listSize == sequence){
//            userServiceClient.checkIn(new RequestCheckIn(userId, 0,0));
//            findRoute.get().updateDone(true);
//            routeRepository.save(findRoute.get());
//        }
//    }

    public EndWorkDto endWork(int userId,int routeId){ //,int deliveryId
        //if ???????????? work_status??? route_id,delivery_id ???????????? (?????? user?????? delivery_id??? ???????????? ??????..)
        Optional<Route> findRoute = routeRepository.findById(routeId);
        if(findRoute.isPresent()){
            findRoute.get().updateDone(true);
            routeRepository.save(findRoute.get());
            userServiceClient.endWork(userId);

            // ?????? ????????? ????????? ??????
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

    // ????????? ??????
    public CheckinResponseDto checkIn(int userId, CheckInDto checkInDto) throws Exception {
        Optional<Delivery> findDelivery = deliveryRepository.findById(checkInDto.getDeliveryId());
        if(findDelivery.isPresent()){
            findDelivery.get().checkIn(true, checkInDto.getImg());
            deliveryRepository.save(findDelivery.get());
            Optional<Delivery> nextDelivery = deliveryRepository.findByRouteIdAndSequence(findDelivery.get().getRoute().getId(), findDelivery.get().getSequence()+1);
            if(nextDelivery.isPresent()){
                // user?????? ???????????? feign client ?????? ??????
//                userServiceClient.checkIn(new RequestCheckIn(userId,nextDelivery.get().getId()));
                userServiceClient.checkIn(new RequestCheckIn(userId,checkInDto.getDeliveryId()+1));
                return CheckinResponseDto.builder().deliveryId(nextDelivery.get().getId()).build();
            } else {
                return CheckinResponseDto.builder().deliveryId(-1).build();
            }
        } else {
            throw new IllegalStateException("?????? deliveryID??? ???????????? ????????????.");
        }
    }

    @Override
    public List<RouteByRegionAndDateDto> getRouteByRegionAndDate(Region region, LocalDate localDate) throws Exception {
        Iterable<Route> findRouteList = routeRepository.findByRegionAndDate(region, localDate);
        List<RouteByRegionAndDateDto> list = new ArrayList<>();
        HashMap<String, Integer> univ = new HashMap<>();
        findRouteList.forEach(route -> {
            log.info("getRouteByRegionAndDate ?????? route: {}", route);
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
            // ?????? ?????? ????????? ????????? delivery ?????? ??? ??????
            List<Delivery> deliveryList = deliveryRepository.findByRouteId(responseDriverRoute.getRoute_id());
            // ?????? ????????? ?????? delivery??? sequence ??????
            Optional<Delivery> findDelivery = deliveryRepository.findById(responseDriverRoute.getDelivery_id());
            if(deliveryList == null || deliveryList.size() == 0) return DriverRouteAndDeliveryDto.builder().route_id(0).delivery_id(0).sequence(0).all_delvery(0).build();
            if(findDelivery.isPresent()){
                return DriverRouteAndDeliveryDto.builder().route_id(responseDriverRoute.getRoute_id()).delivery_id(responseDriverRoute.getDelivery_id())
                        .sequence(findDelivery.get().getSequence()).all_delvery(deliveryList.size()).build();
            } else {
                throw new Exception("DB ?????? ??????");
            }
        } else {
            throw new IllegalStateException("?????? ???????????? ??????????????? ????????????.");
        }
    }


    public String getDeliveryName(int deliveryId) throws Exception {
        Optional<Delivery> findDelivery = deliveryRepository.findById(deliveryId);
        if(findDelivery.isPresent()){
            return findDelivery.get().getDelName();
        }else{
            throw new Exception("id : "+deliveryId+ " ??? ?????? ???????????? ????????????.");
        }
    }

    public LocalTime getRouteActualStartTime(int routeId) throws Exception {
        Optional<Route> findRoute = routeRepository.findById(routeId);
        if(findRoute.isPresent()){
            return findRoute.get().getActualStartTime();
        }else{
            throw new Exception("id : "+routeId+ " ??? ?????? ????????? ????????????.");
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
