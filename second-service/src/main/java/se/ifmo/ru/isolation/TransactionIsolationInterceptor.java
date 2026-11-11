package se.ifmo.ru.isolation;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import se.ifmo.ru.enums.IsolationType;

@Interceptor
@TransactionIsolation(IsolationType.READ_COMMITTED)
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionIsolationInterceptor {

    @Inject
    InfraStruct infraStruct;

    @AroundInvoke
    public Object setIsolation(InvocationContext context) throws Exception {
        TransactionIsolation annotation = context.getMethod().getAnnotation(TransactionIsolation.class);
        IsolationType isolationType = annotation.value();
        infraStruct.setTransaction(isolationType);
        return context.proceed();
    }
}