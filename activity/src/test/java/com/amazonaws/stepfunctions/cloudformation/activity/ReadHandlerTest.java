package com.amazonaws.stepfunctions.cloudformation.activity;

import com.amazonaws.services.stepfunctions.model.DescribeActivityRequest;
import com.amazonaws.services.stepfunctions.model.DescribeActivityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends HandlerTestBase {

    private ReadHandler handler = new ReadHandler();

    private ResourceHandlerRequest<ResourceModel> request;

    @BeforeEach
    public void setup() {
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .region(REGION)
                .awsAccountId(AWS_ACCOUNT_ID)
                .desiredResourceState(ResourceModel.builder()
                        .arn(ACTIVITY_ARN)
                        .name(ACTIVITY_NAME)
                        .build()
                )
                .build();
    }

    @Test
    public void testSuccess() {
        DescribeActivityResult describeActivityResult = new DescribeActivityResult();
        describeActivityResult.setName(ACTIVITY_NAME);
        describeActivityResult.setActivityArn(ACTIVITY_ARN);

        Mockito.when(proxy.injectCredentialsAndInvoke(Mockito.any(DescribeActivityRequest.class), Mockito.any(Function.class))).thenReturn(describeActivityResult);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(ACTIVITY_ARN);
        assertThat(response.getResourceModel().getName()).isEqualTo(ACTIVITY_NAME);
    }

    @Test
    public void test500() {
        Mockito.when(proxy.injectCredentialsAndInvoke(Mockito.any(DescribeActivityRequest.class), Mockito.any(Function.class))).thenThrow(exception500);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo(exception500.getMessage());
    }

    @Test
    public void testResourceArnIsNull_returnsNotFound() {
        request.setDesiredResourceState(ResourceModel.builder()
                .name(ACTIVITY_NAME)
                .build());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getMessage()).contains(Constants.ACTIVITY_ARN_NOT_FOUND_MESSAGE);
    }

}
