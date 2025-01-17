package de.tum.cit.aet.artemis.programming.hestia.behavioral;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.cit.aet.artemis.assessment.domain.Visibility;
import de.tum.cit.aet.artemis.core.domain.Course;
import de.tum.cit.aet.artemis.programming.AbstractProgrammingIntegrationLocalCILocalVCTestBase;
import de.tum.cit.aet.artemis.programming.domain.ProgrammingExercise;
import de.tum.cit.aet.artemis.programming.domain.ProgrammingExerciseTestCase;
import de.tum.cit.aet.artemis.programming.domain.ProgrammingLanguage;
import de.tum.cit.aet.artemis.programming.domain.hestia.CoverageFileReport;
import de.tum.cit.aet.artemis.programming.domain.hestia.CoverageReport;
import de.tum.cit.aet.artemis.programming.domain.hestia.ProgrammingExerciseGitDiffEntry;
import de.tum.cit.aet.artemis.programming.domain.hestia.ProgrammingExerciseGitDiffReport;
import de.tum.cit.aet.artemis.programming.domain.hestia.ProgrammingExerciseSolutionEntry;
import de.tum.cit.aet.artemis.programming.domain.hestia.ProgrammingExerciseTestCaseType;
import de.tum.cit.aet.artemis.programming.domain.hestia.TestwiseCoverageReportEntry;
import de.tum.cit.aet.artemis.programming.util.LocalRepository;

class BehavioralTestCaseServiceLocalCILocalVCTest extends AbstractProgrammingIntegrationLocalCILocalVCTestBase {

    private static final String TEST_PREFIX = "behavioraltestcastservice";

    private final LocalRepository solutionRepo = new LocalRepository("main");

    private ProgrammingExercise exercise;

    @Override
    protected String getTestPrefix() {
        return TEST_PREFIX;
    }

    @BeforeEach
    void initTestCase() {
        userUtilService.addUsers(TEST_PREFIX, 0, 0, 0, 1);
        final Course course = programmingExerciseUtilService.addCourseWithOneProgrammingExercise(false, true, ProgrammingLanguage.JAVA);
        exercise = exerciseUtilService.getFirstExerciseWithType(course, ProgrammingExercise.class);
        exercise.getBuildConfig().setTestwiseCoverageEnabled(true);
    }

    @AfterEach
    void cleanup() throws IOException {
        solutionRepo.resetLocalRepo();
    }

    private ProgrammingExerciseTestCase addTestCaseToExercise(String name) {
        var testCase = new ProgrammingExerciseTestCase();
        testCase.setTestName(name);
        testCase.setExercise(exercise);
        testCase.setVisibility(Visibility.ALWAYS);
        testCase.setActive(true);
        testCase.setWeight(1D);
        testCase.setType(ProgrammingExerciseTestCaseType.BEHAVIORAL);
        return testCaseRepository.save(testCase);
    }

    private ProgrammingExerciseGitDiffReport newGitDiffReport() {
        var gitDiffReport = new ProgrammingExerciseGitDiffReport();
        gitDiffReport.setEntries(new HashSet<>());
        gitDiffReport.setProgrammingExercise(exercise);
        gitDiffReport.setSolutionRepositoryCommitHash("123a");
        gitDiffReport.setTemplateRepositoryCommitHash("123b");
        gitDiffReport = reportRepository.save(gitDiffReport);
        return gitDiffReport;
    }

    private ProgrammingExerciseGitDiffReport addGitDiffEntry(String filePath, int startLine, int lineCount, ProgrammingExerciseGitDiffReport gitDiffReport) {
        var gitDiffEntry = new ProgrammingExerciseGitDiffEntry();
        gitDiffEntry.setFilePath(filePath);
        gitDiffEntry.setStartLine(startLine);
        gitDiffEntry.setLineCount(lineCount);
        gitDiffEntry.setGitDiffReport(gitDiffReport);
        gitDiffReport.getEntries().add(gitDiffEntry);
        return reportRepository.save(gitDiffReport);
    }

    private CoverageReport newCoverageReport() {
        var solutionParticipation = solutionProgrammingExerciseRepository.findWithEagerResultsAndSubmissionsByProgrammingExerciseId(exercise.getId()).orElseThrow();
        var solutionSubmission = programmingExerciseUtilService.createProgrammingSubmission(solutionParticipation, false);

        var coverageReport = new CoverageReport();
        coverageReport.setFileReports(new HashSet<>());
        coverageReport.setSubmission(solutionSubmission);
        coverageReport = coverageReportRepository.save(coverageReport);
        return coverageReport;
    }

    private CoverageFileReport newCoverageFileReport(String filePath, CoverageReport coverageReport) {
        var coverageFileReport = new CoverageFileReport();
        coverageFileReport.setFilePath(filePath);
        coverageFileReport.setTestwiseCoverageEntries(new HashSet<>());
        coverageFileReport.setFullReport(coverageReport);
        coverageFileReport = coverageFileReportRepository.save(coverageFileReport);
        coverageReport.getFileReports().add(coverageFileReport);
        return coverageFileReport;
    }

    private TestwiseCoverageReportEntry newCoverageReportEntry(int startLine, int lineCount, ProgrammingExerciseTestCase testCase, CoverageFileReport coverageFileReport) {
        var coverageReportEntry = new TestwiseCoverageReportEntry();
        coverageReportEntry.setTestCase(testCase);
        coverageReportEntry.setStartLine(startLine);
        coverageReportEntry.setLineCount(lineCount);
        coverageReportEntry.setFileReport(coverageFileReport);
        coverageReportEntry = testwiseCoverageReportEntryRepository.save(coverageReportEntry);
        coverageFileReport.getTestwiseCoverageEntries().add(coverageReportEntry);
        return coverageReportEntry;
    }

    @Test
    @WithMockUser(username = TEST_PREFIX + "instructor1", roles = "INSTRUCTOR")
    void testGenerationForSimpleExample() throws Exception {
        exercise = hestiaUtilTestService.setupSolution("Test.java", "A\nB\nC\nD\nE\nF\nG\nH", exercise, solutionRepo);
        var testCase = addTestCaseToExercise("testCase");

        var gitDiffReport = newGitDiffReport();
        addGitDiffEntry("Test.java", 2, 7, gitDiffReport);

        var coverageReport = newCoverageReport();
        var coverageFileReport = newCoverageFileReport("Test.java", coverageReport);
        newCoverageReportEntry(1, 3, testCase, coverageFileReport);
        newCoverageReportEntry(5, 2, testCase, coverageFileReport);

        var solutionEntries = behavioralTestCaseService.generateBehavioralSolutionEntries(exercise);

        var expected1 = new ProgrammingExerciseSolutionEntry();
        expected1.setId(0L);
        expected1.setFilePath("Test.java");
        expected1.setTestCase(testCase);
        expected1.setLine(2);
        expected1.setCode("B\nC");
        var expected2 = new ProgrammingExerciseSolutionEntry();
        expected2.setId(0L);
        expected2.setFilePath("Test.java");
        expected2.setTestCase(testCase);
        expected2.setLine(5);
        expected2.setCode("E\nF");
        assertThat(solutionEntries).isNotNull();
        solutionEntries.forEach(solutionEntry -> solutionEntry.setId(0L));
        assertThat(solutionEntries).containsExactlyInAnyOrder(expected1, expected2);
    }
}
