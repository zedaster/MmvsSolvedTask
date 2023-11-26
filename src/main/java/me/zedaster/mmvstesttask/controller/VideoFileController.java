package me.zedaster.mmvstesttask.controller;

import me.zedaster.mmvstesttask.MmvsTestApplication;
import me.zedaster.mmvstesttask.controller.dto.AdjustDto;
import me.zedaster.mmvstesttask.controller.exception.BadRequestException;
import me.zedaster.mmvstesttask.controller.response.BadRequestResponse;
import me.zedaster.mmvstesttask.controller.response.SuccessResponse;
import me.zedaster.mmvstesttask.controller.response.UploadResponse;
import me.zedaster.mmvstesttask.repository.video.NonExistentIdException;
import me.zedaster.mmvstesttask.repository.video.ProcessingException;
import me.zedaster.mmvstesttask.service.video.FileInfo;
import me.zedaster.mmvstesttask.service.video.FutureVideo;
import me.zedaster.mmvstesttask.service.video.VideoFileService;
import me.zedaster.mmvstesttask.service.video.exception.IncorrectSizeException;
import me.zedaster.mmvstesttask.service.video.exception.UploadFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Обработчик REST операций, связанных c хранением видеофайлов
 */
@RestController
public class VideoFileController {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MmvsTestApplication.class);

    /**
     * Сервис хранения видеофайлов с возможностью менять разрешение
     */
    private final VideoFileService videoFileService;

    @Autowired
    public VideoFileController(VideoFileService videoFileService) {
        this.videoFileService = videoFileService;
    }

    /**
     * Отправляет видеофайл (в формате MP4) на загрузку. В ответ возвращает сгенерированный UUID
     * @return Объект UploadResponse, содержащий uuid загруженного файла. HTTP код ответа будет 200
     * @throws BadRequestException если формат файла некорректный. HTTP код ответа будет 400
     */
    @PostMapping("file/")
    // TODO add file here
    public UploadResponse saveFile(@RequestParam("file") MultipartFile file) {
        try {
            FutureVideo futureVideo = videoFileService.asyncSaveFile(file.getOriginalFilename(), file);
            return new UploadResponse(futureVideo.getUuid());
        } catch (UploadFormatException e) {
            throw new BadRequestException("Формат файла неверный!");
        }
    }

    /**
     * Изменяет размер загруженного видеофайла
     * @param uuidValue UUID загруженного видеофайла
     * @param adjustDto Содержит необходимые даннны для запроса (ширину и длину)
     * @return SuccessResponse с кодом 200
     * @throws BadRequestResponse если введен неверный UUID или некорректные размеры. HTTP код ответа будет 400
     */
    @PatchMapping("file/{uuidValue}")
    public SuccessResponse adjustSize(@PathVariable String uuidValue, @RequestBody AdjustDto adjustDto) {
        try {
            UUID uuid = UUID.fromString(uuidValue);
            videoFileService.asyncAdjustSize(uuid, adjustDto.getWidth(), adjustDto.getHeight());
            return new SuccessResponse();
        } catch (IllegalArgumentException | NonExistentIdException e) {
            throw new BadRequestException("Id файла указан неверно!");
        } catch (IncorrectSizeException e) {
            throw new BadRequestException("Указан неверные новые размеры! Каждое измерение должно быть " +
                    "четным числом больше 20.");
        } catch (ProcessingException e) {
            throw new BadRequestException("Файл находиться в процессе обработки. Дождитесь ее окончания");
        }
    }

    /**
     * Возвращает информацию о файле и статусе его обработки
     * @param uuidValue UUID загруженного видеофайла
     * @return Объект, VideoFileInfo содержащий информацию о файле. HTTP код ответа будет 200
     * @throws BadRequestException если введен неверный UUID. HTTP код ответа будет 400
     */
    @GetMapping("file/{uuidValue}")
    @ResponseBody
    public FileInfo getInfoById(@PathVariable String uuidValue) {
        try {
            UUID uuid = UUID.fromString(uuidValue);
            return videoFileService.getInfoById(uuid);
        } catch (NonExistentIdException | IllegalArgumentException e) {
            throw new BadRequestException("Id файла указан неверно!");
        }
    }

    /**
     * Удаляет видеофайл
     * @param uuidValue UUID удаляемого видеофайла
     * @return SuccessResponse с кодом 200
     * @throws BadRequestException если введен неверный UUID. HTTP код ответа будет 400
     */
    @DeleteMapping("file/{uuidValue}")
    public SuccessResponse deleteByUuid(@PathVariable String uuidValue) {
        try {
            UUID uuid = UUID.fromString(uuidValue);
            videoFileService.asyncDeleteById(uuid);
            return new SuccessResponse();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Id файла указан неверно!");
        } catch (NonExistentIdException e) {
            throw new BadRequestException("Такого файла не существует!");
        }
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequestResponse processBadRequest(BadRequestException exception) {
        return exception.toResponse();
    }

}
