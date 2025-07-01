package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import java.time.DayOfWeek
import java.util.*

class DaysOfWeekHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    private fun parseDaysOfWeek(daysOfWeekText: String?): MutableSet<DayOfWeek> {
        val days: MutableSet<DayOfWeek> = mutableSetOf()
        if (daysOfWeekText.isNullOrBlank()) {
            return days // Return empty set if input is null/blank
        }

        val daysArray = daysOfWeekText.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (day in daysArray) {
            // Trim whitespace from each day string before processing
            val trimmedDay = day.trim { it <= ' ' }
            when (trimmedDay.lowercase(Locale.getDefault())) {
                "monday" -> days.add(DayOfWeek.MONDAY)
                "tuesday" -> days.add(DayOfWeek.TUESDAY)
                "wednesday" -> days.add(DayOfWeek.WEDNESDAY)
                "thursday" -> days.add(DayOfWeek.THURSDAY)
                "friday" -> days.add(DayOfWeek.FRIDAY)
                "saturday" -> days.add(DayOfWeek.SATURDAY)
                "sunday" -> days.add(DayOfWeek.SUNDAY)
                "weekdays" -> {
                    days.add(DayOfWeek.MONDAY)
                    days.add(DayOfWeek.TUESDAY)
                    days.add(DayOfWeek.WEDNESDAY)
                    days.add(DayOfWeek.THURSDAY)
                    days.add(DayOfWeek.FRIDAY)
                }

                "weekend" -> {
                    days.add(DayOfWeek.SATURDAY)
                    days.add(DayOfWeek.SUNDAY)
                }

                else ->           // Try to parse as integer (1-7, where 1 is Monday)
                    try {
                        val dayNum = trimmedDay.toInt()
                        if (dayNum in 1..7) {
                            days.add(DayOfWeek.of(dayNum))
                        }
                    } catch (e: NumberFormatException) {
                        // Ignore unparseable values
                    }
            }
        }
        return days
    }

    override fun endElement(currentEntity: Entity) {
        activeDatesRepository.dayTypeToDaysOfWeek[currentEntity.id] = parseDaysOfWeek(stringBuilder.toString())
    }
}