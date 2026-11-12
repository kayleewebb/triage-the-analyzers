package team.projectpulse.instructor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Name and email search folds the caller's text in Java, then compares it with SQL LOWER.
 * That Java fold has to be locale-independent: under a Turkish default locale,
 * {@code "I".toLowerCase()} is {@code "ı"} (U+0131), which does not match {@code "i"}.
 */
@ExtendWith(MockitoExtension.class)
class InstructorSpecsTest {

    @Mock
    CriteriaBuilder criteriaBuilder;

    @Mock
    Root<Instructor> root;

    @Mock
    Path<String> firstName;

    @Mock
    Expression<String> loweredFirstName;


    @Test
    void hasFirstNameFoldsCapitalIToIEvenUnderTurkishLocale() {
        given(root.<String>get("firstName")).willReturn(firstName);
        given(criteriaBuilder.lower(firstName)).willReturn(loweredFirstName);

        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            InstructorSpecs.hasFirstName("I").toPredicate(root, null, criteriaBuilder);

            ArgumentCaptor<Object> searchValue = ArgumentCaptor.forClass(Object.class);
            verify(criteriaBuilder).equal(eq(loweredFirstName), searchValue.capture());
            assertThat(searchValue.getValue())
                    .as("hasFirstName(\"I\") under default locale %s", Locale.getDefault())
                    .isEqualTo("i");
        } finally {
            Locale.setDefault(previous);
        }
    }

}
