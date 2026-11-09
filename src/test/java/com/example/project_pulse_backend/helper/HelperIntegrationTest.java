package com.example.project_pulse_backend.helper;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.ExchangeTokenResponse;
import com.example.project_pulse_backend.dto.response.OutboundUserResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.mapper.NotificationMapper;
import com.example.project_pulse_backend.repository.*;
import com.example.project_pulse_backend.repository.httpClient.*;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HelperIntegrationTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completesGoogleAuthAndSupportsEmailLookup() {
        AuthRepo authRepo = mock(AuthRepo.class);
        OutboundIdentityClient identity = mock(OutboundIdentityClient.class);
        OutboundUserClient userClient = mock(OutboundUserClient.class);
        Auth auth = Auth.builder().email("person@example.com")
                .user(User.builder().roles(List.of(Role.builder().name("PM").build())).build()).build();
        AuthHelper helper = new AuthHelper(authRepo, identity, userClient);
        ReflectionTestUtils.setField(helper, "CLIENT_ID", "client");
        ReflectionTestUtils.setField(helper, "CLIENT_SECRET", "secret");
        ReflectionTestUtils.setField(helper, "REDIRECT_URI", "https://redirect");
        ReflectionTestUtils.setField(helper, "GRANT_TYPES", "authorization_code");
        when(identity.exchangeToken(any())).thenReturn(ExchangeTokenResponse.builder()
                .accessToken("google-token").build());
        when(userClient.getUserDetails("Bearer google-token")).thenReturn(OutboundUserResponse.builder()
                .email("person@example.com").build());
        when(authRepo.existsByEmail("person@example.com")).thenReturn(true);
        when(authRepo.findByEmail("person@example.com")).thenReturn(Optional.of(auth));

        assertSame(auth, helper.authenticateGoogleCode("code"));
        assertSame(auth, helper.getAuthenticationByEmail("person@example.com"));
        assertEquals(List.of("PM"), helper.getRoleNames(auth));
        assertEquals(List.of(), helper.getRoleNames(Auth.builder().user(null).build()));
        assertEquals(36, helper.generateActivationCode().length());
        verify(identity).exchangeToken(argThat(request -> "code".equals(request.getCode())));

        when(authRepo.existsByEmail("person@example.com")).thenReturn(false);
        assertThrows(AppException.class, () -> helper.authenticateGoogleCode("code"));
        when(authRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getAuthenticationByEmail("missing@example.com"));
    }

    @Test
    void getsCurrentAuthAndUserFromAuthenticatedSecurityPrincipal() {
        AuthRepo authRepo = mock(AuthRepo.class);
        GetAuthByToken getAuth = new GetAuthByToken(authRepo);
        GetUserByToken getUser = new GetUserByToken(authRepo);
        User user = User.builder().id(5L).build();
        Auth auth = Auth.builder().email("person@example.com").user(user).build();
        when(authRepo.findByEmail("person@example.com")).thenReturn(Optional.of(auth));

        assertNull(getAuth.get());
        assertNull(getUser.get());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("person@example.com", "password", List.of()));
        assertSame(auth, getAuth.get());
        assertSame(user, getUser.get());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unknown@example.com", "password", List.of()));
        when(authRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(AppException.class, getAuth::get);
        assertThrows(AppException.class, getUser::get);
    }

    @Test
    void persistsNotificationsAndFormatsMessagesAndLinks() {
        NotificationRepository notificationRepo = mock(NotificationRepository.class);
        UserNotificationRepository userNotificationRepo = mock(UserNotificationRepository.class);
        NotificationMapper mapper = mock(NotificationMapper.class);
        NotificationHelper helper = new NotificationHelper(notificationRepo, userNotificationRepo, mapper);
        Notification notification = Notification.builder().build();
        UserNotification userNotification = new UserNotification();
        when(mapper.toNotification(any())).thenReturn(notification);
        when(mapper.toUserNotification(any())).thenReturn(userNotification);
        when(notificationRepo.save(notification)).thenReturn(notification);

        assertSame(notification, helper.createNotification(NotificationRequest.builder().build()));
        helper.createUserNotification(CreateUserNotificationRequest.builder().build());
        verify(notificationRepo).save(notification);
        verify(userNotificationRepo).save(userNotification);
        assertNotNull(userNotification.getCreatedAt());
        assertFalse(userNotification.isRead());

        assertEquals("Alex vừa tạo 1 task note trong task Build ",
                helper.createTaskNoteNotification("Alex", "Build"));
        assertEquals("Bạn vừa nhận được 2 point từ task Build ",
                helper.getPointFormTaskNotification(2.0, "Build"));
        assertEquals("Bạn vừa nhận được 0 point từ task Build ",
                helper.getPointFormTaskNotification(null, "Build"));
        assertEquals("Bạn vừa được giao task Build ", helper.assignTaskNotification("Build"));
        assertEquals("Bạn vừa bị chuyển 1.25 point từ task Build sang cho Alex ",
                helper.transferPointFormTaskNotification(1.25, "Build", "Alex"));
        assertEquals("Bạn vừa được chuyển cho 1.25 point của Alex từ task Build ",
                helper.getTransferFormTaskNotification(1.25, "Build", "Alex"));
        assertEquals("Bạn đã được loại bỏ khỏi task Build ", helper.removeFromTaskNotification("Build"));

        assertEquals("/projects?tab=tasks", helper.linkToTask(null, 2L));
        assertEquals("/projects/4?tab=tasks", helper.linkToTask(4L, null));
        assertEquals("/projects/4?tab=tasks&taskId=2", helper.linkToTask(4L, 2L));
        assertEquals("/projects?tab=earnings", helper.linkToEarn(null, null));
        assertEquals("/projects/4?tab=earnings", helper.linkToEarn(4L, null));
        assertEquals("/projects/4?tab=earnings&transactionId=2", helper.linkToEarn(4L, 2L));
        assertEquals("/projects/4?tab=earnings&type=TRANSFER", helper.linkToTransfer(4L, null));
        assertEquals("/projects/4?tab=earnings&type=TRANSFER&transactionId=2",
                helper.linkToTransfer(4L, 2L));
    }

    @Test
    void uploadsOnlyImagesAndDeletesCloudinaryPublicId() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<?, ?> uploaded = Map.of("secure_url", "https://cloud.example/image.png");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploaded);
        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));
        FileManager manager = new FileManager(cloudinary);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("PHOTO.PNG");
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        assertSame(uploaded, manager.uploadImage(file));
        ArgumentCaptor<Map> uploadOptions = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), uploadOptions.capture());
        assertEquals("image", uploadOptions.getValue().get("resource_type"));
        assertEquals("upload", uploadOptions.getValue().get("type"));
        assertTrue(uploadOptions.getValue().get("public_id").toString().startsWith("tailieu/image/"));
        manager.deleteFile("https://res.cloudinary.com/demo/image/upload/v123456/tailieu/image/a.jpg");
        verify(uploader).destroy(eq("tailieu/image/a"), anyMap());

        when(file.getOriginalFilename()).thenReturn("notes.pdf");
        assertThrows(AppException.class, () -> manager.uploadImage(file));
        when(file.getOriginalFilename()).thenReturn(null);
        assertThrows(AppException.class, () -> manager.uploadImage(file));
    }

    @Test
    void sendsEmailAndTranslatesFeignFailure() {
        EmailClient client = mock(EmailClient.class);
        EmailHelper helper = new EmailHelper(client);
        ReflectionTestUtils.setField(helper, "apiKey", "api-key");
        ReflectionTestUtils.setField(helper, "senderEmail", "noreply@example.com");
        ReflectionTestUtils.setField(helper, "senderName", "Pulse");
        SendEmailRequest request = SendEmailRequest.builder()
                .to(Recipient.builder().email("to@example.com").name("To").build())
                .subject("Subject").htmlContent("<p>hello</p>").build();

        helper.sendEmail(request);
        verify(client).sendEmail(eq("api-key"), argThat(email ->
                "Subject".equals(email.getSubject())
                        && "noreply@example.com".equals(email.getSender().getEmail())
                        && email.getTo().getFirst().getEmail().equals("to@example.com")));
        FeignException failure = mock(FeignException.class);
        doThrow(failure).when(client).sendEmail(anyString(), any());
        assertThrows(AppException.class, () -> helper.sendEmail(request));
    }
}
