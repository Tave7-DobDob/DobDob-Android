# DobDob-Android

![DobDob_main_UI](https://user-images.githubusercontent.com/78736070/151484058-71a13340-878c-4f76-b9bc-09cc8c5b337b.jpg)
## 동네 주민간의 서로 돕고 돕는 용도의 커뮤니티 서비스

### 개발 환경
- IDE: 안드로이드 스튜디오(Android Studio)

- Language: Java

- Server와의 통신: Retrofit

<br/>

### 주요 기능
- 로그인

  ![image](https://user-images.githubusercontent.com/78736070/151484696-c84155b2-3a7e-49c6-86b4-52beb173c20e.png)
  
  Kakao SDK for Android를 통해 카카오로그인을 통한 불필요한 회원가입 절차 생략으로 <b>편리성 향상</b>
  
  또한 JWT를 사용해 정보의 안전성 확보


- 위치 설정

  ![image](https://user-images.githubusercontent.com/78736070/151484612-b34929a1-0232-4baf-8a81-559135b32bbf.png)
  
  Daum API로 주소 검색을 통한 나의 동네 설정
  
  카카오 로컬 REST API를 이용하여 위도, 경도, 행정구역 추출(시, 구, 동)


- 위치 기반 포스팅

  ![image](https://user-images.githubusercontent.com/78736070/151485068-e2dc0640-a184-4948-948f-46d57c976afc.png)

  포스팅 시, 설정한 위치 (기본설정: 나의 동네)를 기반으로 나의 동네 6Km 이내의 작성 글들이 보여짐.
  
  또한 위치 변경으로 다른 동네의 글도 확인 가능
  
  지역 및 동네별 포스트 분리로 필요한 정보만을 빠르게 탐색 및 확인
  
  
- 포스트 검색

  ![image](https://user-images.githubusercontent.com/78736070/151485314-5ae7c567-8cae-42c4-983c-a77cc30b110b.png)

  포스트 제목 기반 검색
  
  &#35;으로 구분하여 포스트 태그 기반 검색
  
  제목 및 태그에 대한 검색 기능으로 탐색 시간 절약
 
<br/>
  
### 시연 영상
[DobDob 앱 시연 영상 바로가기](https://drive.google.com/file/d/1yg-MM3_afpTBmmpb4VHVlLJ-nu6luSIc/view?usp=sharing/)
