package com.cgvsu.objreader;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	// Константы для обозначения ключевых слов в формате OBJ
	private static final String OBJ_VERTEX_TOKEN = "v";  // Обозначает вершину
	private static final String OBJ_TEXTURE_TOKEN = "vt"; // Обозначает текстурную координату
	private static final String OBJ_NORMAL_TOKEN = "vn";  // Обозначает нормаль
	private static final String OBJ_FACE_TOKEN = "f";     // Обозначает грань

	// Метод для чтения содержимого файла в формате OBJ
	public static Model read(String fileContent) {
		Model result = new Model(); // Объект для хранения прочитанной модели

		int lineInd = 0; // Номер текущей строки, используется для отладки и обработки ошибок
		Scanner scanner = new Scanner(fileContent); // Сканер для чтения строки за строкой
		while (scanner.hasNextLine()) { // Пока есть строки в файле
			final String line = scanner.nextLine(); // Считываем строку
			// Разделяем строку на слова, используя пробелы в качестве разделителей
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) { // Если строка пустая, пропускаем её
				continue;
			}

			final String token = wordsInLine.get(0); // Первое слово - это токен
			wordsInLine.remove(0); // Удаляем токен из списка

			++lineInd; // Увеличиваем индекс строки
			switch (token) {
				// Обработка строки с вершинами
				case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
				// Обработка строки с текстурными координатами
				case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				// Обработка строки с нормалями
				case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
				// Обработка строки с полигонами
				case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd));
				// Игнорируем строки с незнакомыми токенами
				default -> {}
			}
		}

		return result; // Возвращаем прочитанную модель
	}

	// Метод для парсинга строки с вершинами
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)), // Чтение X-координаты
					Float.parseFloat(wordsInLineWithoutToken.get(1)), // Чтение Y-координаты
					Float.parseFloat(wordsInLineWithoutToken.get(2))  // Чтение Z-координаты
			);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd); // Ошибка в формате числа
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd); // Недостаточно аргументов
		}
	}

	// Метод для парсинга строки с текстурными координатами
	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)), // Чтение U-координаты
					Float.parseFloat(wordsInLineWithoutToken.get(1))  // Чтение V-координаты
			);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd); // Ошибка в формате числа
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd); // Недостаточно аргументов
		}
	}

	// Метод для парсинга строки с нормалями
	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)), // Чтение X-координаты нормали
					Float.parseFloat(wordsInLineWithoutToken.get(1)), // Чтение Y-координаты нормали
					Float.parseFloat(wordsInLineWithoutToken.get(2))  // Чтение Z-координаты нормали
			);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd); // Ошибка в формате числа
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd); // Недостаточно аргументов
		}
	}

	// Метод для парсинга строки с полигонами
	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<>(); // Индексы вершин полигона
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<>(); // Индексы текстурных координат
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<>(); // Индексы нормалей

		// Парсим каждую часть описания полигона
		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
		}

		Polygon result = new Polygon(); // Создаем объект полигона
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);
		return result;
	}

	// Метод для парсинга одной части полигона (формата "v/vt/vn")
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/"); // Разделяем строку по символу '/'
			switch (wordIndices.length) {
				case 1 -> {
					// Только вершины
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1); // Индекс вершины
				}
				case 2 -> {
					// Вершины и текстурные координаты
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
				}
				case 3 -> {
					// Вершины, текстурные координаты и нормали
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
					if (!wordIndices[1].equals("")) {
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd); // Неверный формат строки
				}
			}
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd); // Ошибка в формате числа
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd); // Недостаточно аргументов
		}
	}
}
