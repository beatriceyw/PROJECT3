<%-- Created by IntelliJ IDEA. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <title>주식 마스터 - 폼</title>
    <link rel="stylesheet" href="<c:url value='/css/style.css'/>"/>
</head>
<body>

<c:set var="isEdit" value="${not empty prefill}"/>

<c:url var="formAction" value="/stocks/${isEdit ? 'update' : 'create'}"/>

<h1>${isEdit ? '종목 수정' : '종목 추가'}</h1>

<form method="post" action="${formAction}" class="form">

    <c:if test="${isEdit}">
        <!-- PK로 업데이트하기 위해 id를 숨겨서 보냄 -->
        <input type="hidden" name="id" value="${prefill.id}"/>
    </c:if>

    <div class="row">
        <label>종목코드</label>
        <!-- EDIT에서도 더 이상 readonly 아님: 코드 직접 수정 가능 -->
        <input name="stockCode"
               value="${isEdit ? prefill.stockCode : ''}"
               required />
    </div>

    <div class="row">
        <label>종목명</label>
        <input name="stockName"
               value="${isEdit ? prefill.stockName : ''}"
               placeholder="${isEdit ? '비워두면 기존 유지' : ''}" />
    </div>

    <div class="row">
        <label>PBR</label>
        <input name="pbr" type="number" step="0.0001"
               value="${isEdit ? prefill.pbr : ''}"/>
    </div>

    <div class="row">
        <label>PER</label>
        <input name="per" type="number" step="0.0001"
               value="${isEdit ? prefill.per : ''}"/>
    </div>

    <div class="actions">
        <button type="submit">${isEdit ? '수정' : '추가'}</button>
        <a class="button" href="<c:url value='/stocks/list'/>">목록</a>
    </div>
</form>

</body>
</html>
